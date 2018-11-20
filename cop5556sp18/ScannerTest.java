/**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();


	//To make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */

	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(pos, t.pos);
		assertEquals(length, t.length);
		assertEquals(line, t.line());
		assertEquals(pos_in_line, t.posInLine());
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}



	/**
	 * Simple test case with an empty program.  The only Token will be the EOF Token.
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}

	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, the end of line 
	 * character would be inserted by the text editor.
	 * Showing the input will let you check your input is 
	 * what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}



	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failIllegalChar() throws LexicalException {
		String input = ";;~";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}




	@Test
	public void testParens() throws LexicalException {
		String input = "()";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 1, 1, 1, 2);
		checkNextIsEOF(scanner);
	}

	@Test
	public void testeq() throws LexicalException {
		String input = "==";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		//checkNext(scanner, OP_EQ, 1, 1, 1, 2);
		checkNextIsEOF(scanner);
		//checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}

	@Test
	public void checkEqualsIllegal() throws LexicalException {
		String input = "===";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void checkIllegal1() throws LexicalException {
		String input = ">>=";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void checkAllOperations() throws LexicalException {
		String input = "==/* comment here */?:=><>=<=:!=&|+-*/%**:=@";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		checkNext(scanner, Kind.OP_QUESTION, 20, 1, 1, 21);
		//checkNext(scanner, Kind., 30, 1, 1, 1);
		//		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		//		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		//		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		//		show(scanner);

	}

	@Test
	public void checkAllSeprators() throws LexicalException {
		String input = "()/* comment here */{}[];,<<>>";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);

	}

	@Test
	public void checkComment() throws LexicalException {
		String input = "==/* comment here */?:=";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
	}

	@Test
	public void checkCommentIllegal() throws LexicalException {
		String input = "/*";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			//assertEquals(1,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	@Test
	public void checkGT() throws LexicalException {
		String input = ">";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.OP_GT, 0, 1, 1, 1);
	}

	@Test
	public void checkDigit() throws LexicalException {
		String input = "012345";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, Kind.INTEGER_LITERAL, 1, 5, 1, 2);

	}

	@Test
	public void checkDigit1() throws LexicalException {
		String input = "0";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.INTEGER_LITERAL, 0, 1, 1, 1);
	}

	@Test
	public void checkDigit2() throws LexicalException {
		String input = "10";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.INTEGER_LITERAL, 0, 2, 1, 1);
	}

	@Test
	public void checkFl() throws LexicalException {
		String input = "0.12345";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.FLOAT_LITERAL, 0, 7, 1, 1);
	}

	@Test
	public void checkFl1() throws LexicalException {
		String input = "0.12.345";
		show(input);
		//thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
			checkNext(scanner, Kind.FLOAT_LITERAL, 0, 4, 1, 1);
			checkNext(scanner, Kind.FLOAT_LITERAL, 4, 4, 1, 5);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			//assertEquals(1,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void checkFl2() throws LexicalException {
		String input = ".12345";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.FLOAT_LITERAL, 0, 6, 1, 1);
	}

	@Test
	public void checkFl3() throws LexicalException {
		String input = "..12345";
		show(input);
		//thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
			checkNext(scanner, Kind.DOT, 0, 1, 1, 1);
			checkNext(scanner, Kind.FLOAT_LITERAL, 1, 6, 1, 2);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			//assertEquals(1,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void checkId1() throws LexicalException {
		String input = "$aditya_vashist";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void checkId3() throws LexicalException {
		String input = "_aditya_vashist";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void checkId2() throws LexicalException {
		String input = "a_b$c_d$$E__F";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.IDENTIFIER, 0, 13, 1, 1);
	}

	@Test
	public void checkCode() throws LexicalException {
		String input = "{int q:=10 \n int b:= 20 \n if(a>=b ) return tr}";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		checkNext(scanner, Kind.LBRACE, 0, 1, 1, 1);
		checkNext(scanner, Kind.KW_int, 1, 3, 1, 2);
		checkNext(scanner, Kind.IDENTIFIER, 5, 1, 1, 6);
		checkNext(scanner, Kind.OP_ASSIGN, 6, 2, 1, 7);
		checkNext(scanner, Kind.INTEGER_LITERAL, 8, 2, 1, 9);
		checkNext(scanner, Kind.KW_int, 13, 3, 2, 2);
		checkNext(scanner, Kind.IDENTIFIER, 17, 1, 2, 6);
		checkNext(scanner, Kind.OP_ASSIGN, 18, 2, 2, 7);
		checkNext(scanner, Kind.INTEGER_LITERAL, 21, 2, 2, 10);
		checkNext(scanner, Kind.KW_if, 26, 2, 3, 2);
		show(scanner);
	}

	@Test
	public void checkBoolTrue() throws LexicalException {
		String input = "true";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.BOOLEAN_LITERAL, 0, 4, 1, 1);
	}

	@Test
	public void checkBoolFalse() throws LexicalException {
		String input = "false";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.BOOLEAN_LITERAL, 0, 5, 1, 1);
	}

	@Test
	public void checkFloat() throws LexicalException {
		String input = "1.2.3";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.FLOAT_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, Kind.FLOAT_LITERAL, 3, 2, 1, 4);

	}


	@Test
	public void checkIntString() throws LexicalException {
		String input = "ABC0123ABD";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.IDENTIFIER, 0, 10, 1, 1);
//		checkNext(scanner, Kind.INTEGER_LITERAL, 3, 4, 1, 4);
//		checkNext(scanner, Kind.IDENTIFIER, 7, 3, 1, 8);
	}
	
	@Test
	public void checkFloatOverload() throws LexicalException {
		String input = "12345682487459674376576243587436543765843765764357643255476559259485459254677891000000000000000000.862546767765476235625765376527645673265376237634590";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			//assertEquals(1,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void checkIntegerOverload() throws LexicalException {
		String input = "2147483649";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner=new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			//assertEquals(1,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}

	}
	
	@Test
	public void checkfloat() throws LexicalException {
		String input = "321.";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.FLOAT_LITERAL, 0, 4, 1, 1);
	}
	
	@Test
	public void checkStar() throws LexicalException {
		String input = "***\n***";
		show(input);
		Scanner scanner=new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, Kind.OP_POWER, 0, 2, 1, 1);
		checkNext(scanner, Kind.OP_TIMES, 2, 1, 1, 3);
		checkNext(scanner, Kind.OP_POWER, 4, 2, 2, 1);
		checkNext(scanner, Kind.OP_TIMES, 6, 1, 2, 3);
	}


}


