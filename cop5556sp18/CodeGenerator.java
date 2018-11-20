/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */


package cop5556sp18;

import java.awt.image.BufferedImage;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import java.util. ArrayList;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.RuntimeImageSupport;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.CodeGenUtils;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	int slotNumber;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		this.slotNumber=1;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		Label startLabel= new Label();
		mv.visitLabel(startLabel);

		Label endLabel= new Label();
		mv.visitLabel(endLabel);

		ArrayList<Label> labels = new ArrayList<Label>();
		labels.add(startLabel);
		labels.add(endLabel);

		for (ASTNode node : block.decsOrStatements) {
			node.visit(this, labels);
		}
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
					throws Exception {

		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;

	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {

		String name = declaration.name;
		Type type = Types.getType(declaration.type);
		declaration.setSlotNumber(this.slotNumber++);

		@SuppressWarnings("unchecked")
		ArrayList<Label> labels=(ArrayList<Label>)arg;

		mv.visitLocalVariable(name, Types.getASMType(type), null, labels.get(0), labels.get(1), declaration.getSlotNumber());

		if(type==Type.IMAGE){
			if(declaration.height!=null && declaration.width!=null){
				declaration.width.visit(this, arg);
				declaration.height.visit(this, arg);
				//				mv.visitVarInsn(ALOAD, declaration.dec.getSlotNumber());"(II)Ljava/awt/image/BufferedImage;"
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", "(II)Ljava/awt/image/BufferedImage;", false);
				mv.visitVarInsn(ASTORE, declaration.getSlotNumber());
			}
			else if(declaration.height==null && declaration.width==null){
				mv.visitLdcInsn(this.defaultWidth);
				mv.visitLdcInsn(this.defaultHeight);
				//				mv.visitVarInsn(ALOAD, declaration.dec.getSlotNumber());
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", "(II)Ljava/awt/image/BufferedImage;", false);
				mv.visitVarInsn(ASTORE, declaration.getSlotNumber());
			}
			//			mv.visitInssn(DUP);
			//			mv.visitFieldInsn(PUTFIELD, this.className, name, "Ljava/awt/image/BufferedImage;");
		}
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		
//		Label startExpression = new Label();
		Label endExpression = new Label();
		Label setTrue = new Label();
		
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		if(expressionBinary.leftExpression.type==Type.INTEGER && expressionBinary.rightExpression.type==Type.INTEGER){
			if(expressionBinary.op==Kind.OP_PLUS){
				mv.visitInsn(IADD);
			}
			else if(expressionBinary.op==Kind.OP_MINUS){
				mv.visitInsn(ISUB);
			}
			else if(expressionBinary.op==Kind.OP_MOD){
				mv.visitInsn(IREM);
			}
			else if(expressionBinary.op==Kind.OP_POWER){
				//				mv.visitInsn(IADD);
				mv.visitInsn(POP);
				mv.visitInsn(POP);
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2I);
			}
			else if(expressionBinary.op==Kind.OP_DIV){
				mv.visitInsn(IDIV);
			}
			else if(expressionBinary.op==Kind.OP_TIMES){
				mv.visitInsn(IMUL);
			}
			else if(expressionBinary.op==Kind.OP_AND){
				mv.visitInsn(IAND);
			}
			else if(expressionBinary.op==Kind.OP_OR){
				mv.visitInsn(IOR);
			}
			else if(expressionBinary.op==Kind.OP_NEQ) {
				mv.visitJumpInsn(IF_ICMPNE, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_EQ) {
				mv.visitJumpInsn(IF_ICMPEQ, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_GT) {
				mv.visitJumpInsn(IF_ICMPGT, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_GE) {
				mv.visitJumpInsn(IF_ICMPGE, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_LT) {
				mv.visitJumpInsn(IF_ICMPLT, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_LE) {
				mv.visitJumpInsn(IF_ICMPLE, setTrue);
				mv.visitLdcInsn(false);
			}
		}
		if(expressionBinary.leftExpression.type==Type.FLOAT && expressionBinary.rightExpression.type==Type.FLOAT){
			if(expressionBinary.op==Kind.OP_PLUS){
				mv.visitInsn(FADD);
			}
			else if(expressionBinary.op==Kind.OP_MINUS){
				mv.visitInsn(FSUB);
			}
			else if(expressionBinary.op==Kind.OP_POWER){
				mv.visitInsn(POP);
				mv.visitInsn(POP);
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else if(expressionBinary.op==Kind.OP_DIV){
				mv.visitInsn(FDIV);
			}
			else if(expressionBinary.op==Kind.OP_TIMES){
				mv.visitInsn(FMUL);
			}
			else if(expressionBinary.op==Kind.OP_NEQ) {
				mv.visitInsn(FCMPL);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(IF_ICMPNE, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_EQ) {
				mv.visitInsn(FCMPL);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(IF_ICMPEQ, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_GT) {
				mv.visitInsn(FCMPG);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(IF_ICMPEQ, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_GE) {
				mv.visitInsn(FCMPG);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(IF_ICMPGE, setTrue);
//				mv.visitInsn(POP);				
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_LT) {
				mv.visitInsn(FCMPG);
				mv.visitInsn(ICONST_1);
				mv.visitInsn(INEG);
				mv.visitJumpInsn(IF_ICMPEQ, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_LE) {
				mv.visitInsn(FCMPG);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(IF_ICMPLE, setTrue);
				mv.visitLdcInsn(false);
			}
		}
		
		if((expressionBinary.leftExpression.type==Type.INTEGER && expressionBinary.rightExpression.type==Type.FLOAT)
				|| (expressionBinary.leftExpression.type==Type.FLOAT && expressionBinary.rightExpression.type==Type.INTEGER)){
			mv.visitInsn(POP);
			mv.visitInsn(POP);
			expressionBinary.leftExpression.visit(this, arg);
			if(expressionBinary.leftExpression.type==Type.INTEGER) mv.visitInsn(I2F);
			expressionBinary.rightExpression.visit(this, arg);
			if(expressionBinary.rightExpression.type==Type.INTEGER) mv.visitInsn(I2F);

			if(expressionBinary.op==Kind.OP_PLUS){
				mv.visitInsn(FADD);
			}
			else if(expressionBinary.op==Kind.OP_MINUS){
				mv.visitInsn(FSUB);
			}
			else if(expressionBinary.op==Kind.OP_POWER){
				mv.visitInsn(POP);
				mv.visitInsn(POP);
				expressionBinary.leftExpression.visit(this, arg);
				if(expressionBinary.leftExpression.type==Type.INTEGER) mv.visitInsn(I2D);
				else mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				if(expressionBinary.rightExpression.type==Type.INTEGER) mv.visitInsn(I2D);
				else mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(Opcodes.D2F);
			}
			else if(expressionBinary.op==Kind.OP_DIV){
				mv.visitInsn(Opcodes.FDIV);
			}
			else if(expressionBinary.op==Kind.OP_TIMES){
				mv.visitInsn(Opcodes.FMUL);
			}
		}
		
		if(expressionBinary.leftExpression.type==Type.BOOLEAN && expressionBinary.rightExpression.type==Type.BOOLEAN){
			if(expressionBinary.op==Kind.OP_AND){
				mv.visitInsn(Opcodes.IAND);
			}
			else if(expressionBinary.op==Kind.OP_OR){
				mv.visitInsn(Opcodes.IOR);
			}
			else if(expressionBinary.op==Kind.OP_NEQ) {
				mv.visitJumpInsn(IF_ICMPNE, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_EQ) {
				mv.visitJumpInsn(IF_ICMPEQ, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_GT) {
				mv.visitJumpInsn(IF_ICMPGT, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_GE) {
				mv.visitJumpInsn(IF_ICMPGE, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_LT) {
				mv.visitJumpInsn(IF_ICMPLT, setTrue);
				mv.visitLdcInsn(false);
			}
			else if(expressionBinary.op==Kind.OP_LE) {
				mv.visitJumpInsn(IF_ICMPLE, setTrue);
				mv.visitLdcInsn(false);
			}
		}
		
		mv.visitJumpInsn(GOTO, endExpression);
		mv.visitLabel(setTrue);
		mv.visitLdcInsn(true);
		mv.visitLabel(endExpression);
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
					throws Exception {
		// TODO Auto-generated method stub
		
//		throw new UnsupportedOperationException();
		Label start = new Label();
		Label exp1 = new Label();
		Label exp2 = new Label();
		Label end = new Label();
		
		mv.visitLabel(start);		
		expressionConditional.guard.visit(this, arg);		
		mv.visitJumpInsn(IFEQ, exp2); 
		
		mv.visitLabel(exp1);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, end);		
		
		mv.visitLabel(exp2);
		expressionConditional.falseExpression.visit(this, arg);
		
		mv.visitLabel(end);

		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
					throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
		//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {

		expressionFunctionAppWithExpressionArg.e.visit(this, arg);

		if(expressionFunctionAppWithExpressionArg.function==Kind.KW_sin){
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_cos){
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_atan){
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_abs){
			if(expressionFunctionAppWithExpressionArg.e.type==Type.INTEGER)
				mv.visitInsn(I2D);
			if(expressionFunctionAppWithExpressionArg.e.type==Type.FLOAT)
				mv.visitInsn(F2D);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(D)D", false);

			if(expressionFunctionAppWithExpressionArg.e.type==Type.INTEGER)
				mv.visitInsn(D2I);
			if(expressionFunctionAppWithExpressionArg.e.type==Type.FLOAT)
				mv.visitInsn(D2F);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_log){

			if(expressionFunctionAppWithExpressionArg.e.type==Type.INTEGER)
				mv.visitInsn(I2D);
			if(expressionFunctionAppWithExpressionArg.e.type==Type.FLOAT)
				mv.visitInsn(F2D);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);

			if(expressionFunctionAppWithExpressionArg.e.type==Type.INTEGER)
				mv.visitInsn(D2I);
			if(expressionFunctionAppWithExpressionArg.e.type==Type.FLOAT)
				mv.visitInsn(D2F);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_int) {
			if(expressionFunctionAppWithExpressionArg.e.type==Type.FLOAT)	
				mv.visitInsn(F2I);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_float) {
			if(expressionFunctionAppWithExpressionArg.e.type==Type.INTEGER)
				mv.visitInsn(I2F);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_alpha) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getAlpha", RuntimePixelOps.getAlphaSig, false);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_red) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getRed", RuntimePixelOps.getRedSig, false);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_blue) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getBlue", RuntimePixelOps.getBlueSig, false);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_green) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", RuntimePixelOps.getGreenSig, false);
		}

		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_green) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", RuntimePixelOps.getGreenSig, false);
		}

		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_green) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", RuntimePixelOps.getGreenSig, false);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_height) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight", RuntimeImageSupport.getHeightSig, false);
		}
		else if(expressionFunctionAppWithExpressionArg.function==Kind.KW_width) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig, false);
		}

		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {

		if(expressionFunctionAppWithPixel.name == Kind.KW_cart_x) {
			//cart_x( r, theta) =  r * Math.cos(theta)

			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(F2D);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);

			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}
		else if(expressionFunctionAppWithPixel.name == Kind.KW_cart_y) {
			//cart_y(r, theta) =  r * Math.sin(theta)

			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(F2D);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);

			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}

		else if(expressionFunctionAppWithPixel.name == Kind.KW_polar_a) {
			//polar_a(x,y) =  Math.atan2(y, x);

			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(I2D);

			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(I2D);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
			mv.visitInsn(D2F);
		}

		else if(expressionFunctionAppWithPixel.name == Kind.KW_polar_r) {
			//polar_r(x,y) =  Math.hypot(x,y);
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(I2D);

			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(I2D);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
			mv.visitInsn(D2F);
		}

		return null;
		//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = expressionIdent.dec;

		if(Types.getType(dec.type)==Type.INTEGER ||  Types.getType(dec.type)==Type.BOOLEAN)
			mv.visitVarInsn(ILOAD, dec.getSlotNumber());

		else if(Types.getType(dec.type)==Type.FLOAT)
			mv.visitVarInsn(FLOAD, dec.getSlotNumber());

		else
			mv.visitVarInsn(ALOAD, dec.getSlotNumber());		

		return null;

	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
					throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {

		mv.visitVarInsn(ALOAD, expressionPixel.dec.getSlotNumber());		
		expressionPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", RuntimeImageSupport.getPixelSig, false);

		return null;

	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
					throws Exception {

		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);

		mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel", RuntimePixelOps.makePixelSig, false);

		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
					throws Exception {
		// TODO Auto-generated method stub
		//		throw new UnsupportedOperationException();
		if(expressionPredefinedName.name==Kind.KW_Z){
			mv.visitLdcInsn(255);
			//			expressionPredefinedName.dec.getSlotNumber();
		}
		else if(expressionPredefinedName.name==Kind.KW_default_height){
			mv.visitLdcInsn(this.defaultHeight);
		}
		else if(expressionPredefinedName.name==Kind.KW_default_width){
			mv.visitLdcInsn(this.defaultWidth);		
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		//		expressionUnary.
		//		if(expressionUnary.op==Kind.OP_MINUS){
		//			mv.visitInsn(INEG);
		//		}
		//		else 
		if(expressionUnary.type==Type.INTEGER &&expressionUnary.op==Kind.OP_MINUS){
			mv.visitInsn(INEG);
		}
		else if(expressionUnary.type==Type.FLOAT && expressionUnary.op==Kind.OP_MINUS){
			mv.visitInsn(FNEG);
		}
		else if(expressionUnary.op==Kind.OP_EXCLAMATION){
			if(expressionUnary.type==Type.BOOLEAN){
				//				mv.visitInsn(INEG);
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IXOR);
			}
			else{
				mv.visitInsn(INEG);
				mv.visitInsn(ICONST_1);
				mv.visitInsn(ISUB);
			}
		}
		return null;
		//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		Type type=lhsIdent.type;

		switch(type){
		case INTEGER :{
			mv.visitVarInsn(ISTORE, lhsIdent.dec.getSlotNumber());
		}
		break;

		case IMAGE :{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			mv.visitVarInsn(ASTORE, lhsIdent.dec.getSlotNumber());
		}
		break;
		case BOOLEAN:{
			mv.visitVarInsn(ISTORE, lhsIdent.dec.getSlotNumber());
		}
		break;
		case FLOAT :{
			mv.visitVarInsn(FSTORE, lhsIdent.dec.getSlotNumber());
		}
		break;
		}
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {

		mv.visitVarInsn(ALOAD, lhsPixel.dec.getSlotNumber());
		lhsPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig, false);

		return null;

	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {

		mv.visitVarInsn(ALOAD, lhsSample.dec.getSlotNumber());
		lhsSample.pixelSelector.visit(this, arg);

		switch (lhsSample.color) {
			case KW_alpha:{
				mv.visitLdcInsn(RuntimePixelOps.ALPHA);
			}
			break;
			case KW_red:{
				mv.visitLdcInsn(RuntimePixelOps.RED);
			}
			break;
			case KW_green:{
				mv.visitLdcInsn(RuntimePixelOps.GREEN);
			}
			break;
			case KW_blue:{
				mv.visitLdcInsn(RuntimePixelOps.BLUE);
			}
			break;
			default:
				break;
		}
		
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);

		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {

		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
//		if(pixelSelector.ex.type == Type.FLOAT) {
//			mv.visitInsn(POP);
//			mv.visitInsn(F2I);
//			pixelSelector.ey.visit(this, arg);
//			mv.visitInsn(F2I);
//		}
		
		if (pixelSelector.ex.type == Types.Type.FLOAT && pixelSelector.ey.type == Types.Type.FLOAT) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
			pixelSelector.ex.visit(this, arg);
			pixelSelector.ey.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}
		

		return null;

	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		//		 cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {

		Label startIf = new Label();
		Label endIf = new Label();

		statementIf.guard.visit(this, arg); 				
		mv.visitJumpInsn(IFEQ, endIf); 
		mv.visitLabel(startIf); 		
		statementIf.b.visit(this, arg);
		
		mv.visitLabel(endIf); 

		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);

		if(Types.getType(statementInput.dec.type)==Type.INTEGER){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitVarInsn(Opcodes.ISTORE, statementInput.dec.getSlotNumber());
		}

		else if(Types.getType(statementInput.dec.type)==Type.FLOAT){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
			mv.visitVarInsn(Opcodes.FSTORE, statementInput.dec.getSlotNumber());
		}

		else if(Types.getType(statementInput.dec.type)==Type.BOOLEAN){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(Opcodes.ISTORE, statementInput.dec.getSlotNumber());
		}

		else if(Types.getType(statementInput.dec.type)==Type.FILE){
			mv.visitVarInsn(Opcodes.ASTORE, statementInput.dec.getSlotNumber());
		}

		else if(Types.getType(statementInput.dec.type)==Type.IMAGE) {
			if(statementInput.dec.height!=null &&statementInput.dec.width!=null){				
				statementInput.dec.width.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				statementInput.dec.height.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			}
			else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage", RuntimeImageSupport.readImageSig, false);
			mv.visitVarInsn(Opcodes.ASTORE, statementInput.dec.getSlotNumber());
		}
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
		case INTEGER : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(I)V", false);
		}
		break;
		case BOOLEAN : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);

			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");

			mv.visitInsn(Opcodes.SWAP);

			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Z)V", false);
			// TODO implement functionality
			//				throw new UnsupportedOperationException();
		}
		break; //commented out because currently unreachable. You will need
		// it.
		case FLOAT : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(F)V", false);
		}
		break; //commented out because currently unreachable. You will need
		// it.
		case IMAGE : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame", "(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
			mv.visitInsn(POP);
		}
		break;
		case FILE : {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V", false);

		}
		break;
		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		statementSleep.duration.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//		throw new UnsupportedOperationException();
		Label guard = new Label();
		Label block = new Label();
		mv.visitJumpInsn(GOTO, guard);
		mv.visitLabel(block);
		statementWhile.b.visit(this, arg); //BODY
		Label endWhileBody = new Label();
		mv.visitLabel(endWhileBody);
		mv.visitLabel(guard);
		statementWhile.guard.visit(this, arg); //GUARD
		Label endWhileGuard = new Label();
		mv.visitLabel(endWhileGuard);
		mv.visitJumpInsn(IFNE, block); //IFNE BODY
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		
		mv.visitVarInsn(ALOAD,statementWrite.sourceDec.getSlotNumber());
		mv.visitVarInsn(ALOAD,statementWrite.destDec.getSlotNumber());
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", RuntimeImageSupport.writeSig, false);

		return null;
	}

}
