package JavaClassPrinter

import pretty.*
import com.sdc.ast.expressions.Expression
import com.sdc.ast.expressions.Constant
import com.sdc.ast.expressions.BinaryExpression
import com.sdc.ast.expressions.UnaryExpression
import com.sdc.ast.expressions.identifiers.Field
import com.sdc.ast.expressions.identifiers.Variable
import com.sdc.ast.controlflow.Statement
import com.sdc.ast.controlflow.Invocation
import com.sdc.ast.controlflow.Assignment
import com.sdc.ast.controlflow.Return
import com.sdc.java.JavaClass
import com.sdc.java.JavaClassField
import com.sdc.java.JavaClassMethod
import com.sdc.ast.controlflow.Throw
import com.sdc.ast.expressions.New
import com.sdc.ast.controlflow.InstanceInvocation
import com.sdc.cfg.Node
import org.hamcrest.core.IsInstanceOf
import com.sdc.cfg.functionalization.FNode
import java.util.ArrayList
import com.sdc.cfg.functionalization.AnonymousClass
import com.sdc.cfg.Switch

var arguments: PrimeDoc = nil()
var myReturnType: String? = ""
var args: PrimeDoc = nil()

fun printExpression(expression: Expression?, nestSize: Int): PrimeDoc =
        when (expression) {
            is Constant -> text(expression.getValue().toString())

            is BinaryExpression -> {
                val opPriority = expression.getPriority()

                val l = expression.getLeft()
                val left = when (l) {
                    is BinaryExpression ->
                        if (opPriority - l.getPriority() < 2)
                            printExpression(l, nestSize)
                        else
                            printExpressionWithBrackets(l, nestSize)
                    else -> printExpression(l, nestSize)
                }

                val r = expression.getRight()
                val right = when (r) {
                    is BinaryExpression ->
                        if (opPriority - r.getPriority() > 0 || opPriority == 3)
                            printExpressionWithBrackets(r, nestSize)
                        else
                            printExpression(r, nestSize)
                    else -> printExpression(r, nestSize)
                }

                group(left / (text(expression.getOperation()) + right))
            }

            is UnaryExpression -> {
                val operand = expression.getOperand()
                val expr = when (operand) {
                    is BinaryExpression ->
                        if (operand.getPriority() < 2)
                            printExpressionWithBrackets(operand, nestSize)
                        else
                            printExpression(operand, nestSize)
                    else -> printExpression(operand, nestSize)
                }
                text(expression.getOperation()) + expr
            }

            is Field -> text(expression.getName())
            is Variable -> text(expression.getName())

            is com.sdc.ast.expressions.Invocation -> {
                var funName = group(text(expression.getFunction() + "("))
                if (expression is InstanceInvocation) {
                    funName = group(text(expression.getVariable()!!.getName() + ".") + funName)
                }
                val args = expression.getArguments()
                if ((args == null) || (args.size == 0))
                    funName + text(")")
                else {
                    var argsDocs = args.take(args.size - 1)
                            .map { arg -> printExpression(arg, nestSize) + text(", ") }
                    var arguments = nest(2 * nestSize, fill(argsDocs + printExpression(args.last, nestSize)))

                    group(funName + arguments + text(")"))
                }
            }

            is New -> group(
                    text("new") + nest(nestSize, line()
                    + printExpression(expression.getConstructor(), nestSize))
            )

            else -> throw IllegalArgumentException("Unknown Expression implementer!")
        }

fun printStatement(statement: Statement, nestSize: Int): PrimeDoc =
        when (statement) {
            is Invocation -> {
                var funName = group(text(statement.getFunction() + "("))
                if (statement is InstanceInvocation) {
                    funName = group(text(statement.getVariable()!!.getName() + ".") + funName)
                }
                val args = statement.getArguments()
                if (args!!.isEmpty())
                    funName + text(")")
                else {
                    var argsDocs = args.take(args.size - 1)
                            .map { arg -> printExpression(arg, nestSize) + text(", ") }
                    var arguments = nest(2 * nestSize, fill(argsDocs + printExpression(args.last as Expression, nestSize)))

                    group(funName + arguments + text(")"))
                }
            }
            is Assignment -> group(
                    (printExpression(statement.getLeft(), nestSize) + text(" ="))
                    + nest(nestSize, line() + printExpression(statement.getRight(), nestSize))
            )
            is Return -> if (statement.getReturnValue() != null)
                group(
                        text("return") + nest(nestSize, line()
                        + printExpression(statement.getReturnValue(), nestSize))
                )
            else
                text("return")
            is Throw -> group(
                    text("throw") + nest(nestSize, line()
                    + printExpression(statement.getThrowObject(), nestSize))
            )

            else -> throw IllegalArgumentException("Unknown Statement implementer!")
        }
fun printAnonymousClass(aClass: AnonymousClass?, nodes: List<Node>?, nestSize: Int): PrimeDoc {
    if (aClass == null)
        return nil()
    else {
        //        val temp = printStatements(aClass.getAssignments(), nestSize)
        val body = lnest(
                nestSize,
                printFNodes(aClass.getFNodes(), nodes, nestSize)
        ) / group(text("}.start(") + args + text(");"))
        return text("return new Object() {") + body
    }
}

fun printFNodes(fnodes: List<FNode>?, nodes: List<Node>?, nestSize: Int): PrimeDoc {
    if ((fnodes == null) || (fnodes.size() == 0))
        return nil()
    else {
        var body = printFNode(fnodes.get(0), nodes!!.get(0), nestSize)
        for (i in 1..fnodes.size - 1) {
            body = body / printFNode(fnodes.get(i), nodes.get(i), nestSize)
        }
        return body
    }
}

fun printFNode(fnode: FNode, node: Node, nestSize: Int): PrimeDoc {
    var variables = fnode.getArguments()
    var vars = variables!!.take(variables!!.size - 1)
            .map { variable -> text(variable.getExtendName() + ", ") }

    var sl = if (fnode.getIndex() == 0) text("private " + myReturnType + "start(") + arguments + text(") {")
    else text("private " + myReturnType + "fnode_" + fnode.getIndex() + "(") + fill(vars + text(variables!!.last!!.getExtendName())) + text(") {")
    if (fnode.getType() == com.sdc.cfg.functionalization.FNode.FType.SIMPLE) {
        var body = printStatements(fnode.getStatements(), nestSize)
        if (!fnode.getBranches()!!.isEmpty()) {
            body = body / printStatement(fnode.getBranches()!!.get(0), nestSize) + text(";")
        }
        return sl + lnest(nestSize, body) / text("}")
    }
    if (fnode.getType() == com.sdc.cfg.functionalization.FNode.FType.IF) {
        var beg = printStatements(fnode.getStatements(), nestSize)
        var body = text("if (...) {") + lnest(nestSize, printStatement(fnode.getBranches()!!.get(0), nestSize) + text(";"))
        body = body / text("} else {") + lnest(nestSize, printStatement(fnode.getBranches()!!.get(1), nestSize) + text(";"))
        return sl + lnest(nestSize, beg / body / text("}")) / text("}")
    }
    if (fnode.getType() == com.sdc.cfg.functionalization.FNode.FType.SWITCH) {
        var beg = printStatements(fnode.getStatements(), nestSize)
        var sw = node as Switch
        var body = text("switch ") + printExpressionWithBrackets(sw.getExpr(), nestSize) + text(" {")
        val ar = sw.getKeys()
        for (i in 0..ar!!.size - 1) {
            body = body / text("case " + ar.get(i) + ":") / printStatement(fnode.getBranches()!!.get(i), nestSize) + text(";")
        }
        body = body / text("default:") / printStatement(fnode.getBranches()!!.get(ar.size), nestSize) + text(";")
        return sl + lnest(nestSize, beg + lnest(nestSize, body) / text("}")) / text("}")
    }
    return group(nil())
}

fun printStatements(statements: List<Statement>?, nestSize: Int): PrimeDoc {
    if ((statements == null) || (statements.size() == 0))
        return nil()
    else {
        var body = printStatement(statements.get(0), nestSize) + text(";")
        for (i in 1..statements.size - 1) {
            body = body / printStatement(statements.get(i), nestSize) + text(";")
        }
        return body
    }
}

fun printExpressionWithBrackets(expression: Expression?, nestSize: Int): PrimeDoc =
        text("(") + printExpression(expression, nestSize) + text(")")

fun printJavaClass(javaClass: JavaClass): PrimeDoc {
    val packageCode = text("package " + javaClass.getPackage() + ";")
    var imports = group(nil())
    for (importName in javaClass.getImports()!!.toArray())
        imports = group(
                imports
                + nest(javaClass.getNestSize(), line() + text("import " + importName + ";"))
        )

    var declaration = group(text(javaClass.getModifier() + javaClass.getType() + javaClass.getName()))

    val superClass = javaClass.getSuperClass()
    if (!superClass!!.isEmpty())
        declaration = group(declaration / text("extends " + superClass))

    val implementedInterfaces = javaClass.getImplementedInterfaces()!!.toArray()
    if (!implementedInterfaces.isEmpty())
        declaration = group(declaration / text("implements " + implementedInterfaces.get(0)))
    for (interface in implementedInterfaces.drop(1)) {
        declaration = group(
                (declaration + text(","))
                + nest(javaClass.getNestSize(), line() + text(interface as String))
        )
    }

    var javaClassCode = group(packageCode + imports / (declaration + text(" {")))

    for (classField in javaClass.getFields()!!.toArray())
        javaClassCode = group(
                javaClassCode
                + nest(javaClass.getNestSize(), line() + printClassField(classField as JavaClassField))
        )

    for (classMethod in javaClass.getMethods()!!.toArray())
        javaClassCode = group(
                javaClassCode
                + nest(javaClass.getNestSize(), line() + printClassMethod(classMethod as JavaClassMethod))
        )

    return group(javaClassCode / text("}"))
}

fun printClassMethod(classMethod: JavaClassMethod): PrimeDoc {
    val declaration = text(classMethod.getModifier() + classMethod.getReturnType() + classMethod.getName() + "(")
    myReturnType = classMethod.getReturnType()

    var throwsExceptions = group(nil())
    val exceptions = classMethod.getExceptions()
    if (!exceptions!!.isEmpty()) {
        throwsExceptions = group(text("throws " + exceptions.get(0)))
        for (exception in exceptions.drop(1)) {
            throwsExceptions = group((throwsExceptions + text(",")) / text(exception))
        }
        throwsExceptions = group(nest(2 * classMethod.getNestSize(), line() + throwsExceptions))
    }


    if (classMethod.getLastLocalVariableIndex() != 0) {
        var variables = classMethod.getParameters()
        var variablesDocs = variables!!.take(variables!!.size - 1)
                .map { variable -> text(variable) + text(", ") }

        var variables2 = classMethod.getParametersWithOnlyNames()
        var temp = variables2!!.take(variables2!!.size - 1)
                .map { variable2 -> text(variable2) + text(", ") }
        args = fill(temp + text(variables2!!.last as String))
        arguments = nest(2 * classMethod.getNestSize(), fill(variablesDocs + text(variables!!.last as String)))
    }

    val body = lnest(
            classMethod.getNestSize(),
            printAnonymousClass(classMethod.getAnonymousClass(), classMethod.getNodes(), classMethod.getNestSize())
    ) / text("}")

    return group(declaration + arguments + text(")") + throwsExceptions + text(" {")) + body
}

fun printClassField(classField: JavaClassField): PrimeDoc =
        text(classField.getModifier() + classField.getType() + classField.getName() + ";")