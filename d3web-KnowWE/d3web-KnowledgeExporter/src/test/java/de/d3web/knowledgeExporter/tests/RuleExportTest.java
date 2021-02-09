package de.d3web.knowledgeExporter.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.knowledgeExporter.txtWriters.RuleWriter;
import de.d3web.textParser.KBTextInterpreter;
import de.d3web.report.Report;
import de.d3web.textParser.casesTable.TextParserResource;


public class RuleExportTest extends TestCase {
	
	private KBTextInterpreter kbTxtInterpreter;
	private Map<String, TextParserResource> input;
	private Map<String, Report> output;
	private KnowledgeBase kb;
	private KnowledgeManager manager;
	private RuleWriter writer;
	
	public void testSimpleRules() {
		String diagnosis = "answer 1\n answer 2\n answer 3";
		String initQuestion = "start";
		String questions = "start\n "
			+ "- question 1 [mc]\n "
			+ "-- yellow\n"
			+ "--- answer 1 (P5)\n"
			+ "-- blue\n"
			+ "--- answer 2 (P7)\n"
			+ "-- red\n"
			+ "--- answer 3 (P7)\n";
		String rules = "WENN (question 1 = yellow UND question 1 = blue)\n"
			+ "DANN answer 3 = P3\n";
		setUpKB(diagnosis, initQuestion, questions, rules);
		System.out.println(writer.writeText());
		System.out.println("TestSimple -------------------");
		assertTrue(writer.writeText().contains(rules));
		
		rules = "WENN (question 1 = yellow UND question 1 = blue) "
			+ "AUSSER question 1 = red\n"
			+ "DANN answer 3 = P3\n";

		
		setUpKB(diagnosis, initQuestion, questions, rules);
		System.out.println(writer.writeText());
		System.out.println("TestSimple -------------------");
		assertTrue(writer.writeText().contains(rules));

	}
	
	public void testComplexRules() {
		String diagnosis = "answer 1\n answer 2\n answer 3\n answer 4\n answer 5\n answer 6";
		String initQuestion = "start";
		String questions = "start\n "
			+ "- question 1 [mc]\n "
			+ "-- yellow\n"
			+ "--- answer 1 (P5)\n"
			+ "-- blue\n"
			+ "--- answer 2 (P7)\n"
			+ "-- red\n"
			+ "--- answer 3 (P7)\n"
			+ "- question 2 [mc]\n "
			+ "-- east\n"
			+ "--- answer 4 (P5)\n"
			+ "-- south\n"
			+ "--- answer 5 (P7)\n"
			+ "-- north\n"
			+ "--- answer 6 (P7)\n";
		String rules = "WENN (((question 1 = yellow UND question 1 = blue) "
			+ "ODER question 2 = east) UND NICHT (question 2 = north))\n"
			+ "DANN answer 3 = P6\n";
		setUpKB(diagnosis, initQuestion, questions, rules);
		
//		System.out.println(writer.writeText());
//		System.out.println("-\n");
//		System.out.println(rules);
		System.out.println(writer.writeText());
		System.out.println("TestComplex -------------------");
		assertTrue(writer.writeText().contains(rules));
	}
	
	public void testAbstractRules() {
		String diagnosis = "dead\n skinny\n normal\n small\n big";
		String initQuestion = "start";
		String questions = "start\n "
			+ "- BMI [num] {BMI} (10 60)\n "
			+ "-- < 13\n"
			+ "--- dead (P5)\n"
			+ "-- [13 20]\n"
			+ "--- skinny (P7)\n"
			+ "-- [21 25]\n"
			+ "--- normal (P7)\n"
			+ "-- > 25\n"
			+ "--- corpulent (P5)\n"
			
			+ "- weight [num] {kg} (0 500)\n "
			+ "-- < 50\n"
			+ "--- small (P5)\n"
			+ "-- [51 100] \n"
			+ "--- normal (P5)\n"
			+ "-- > 100\n"
			+ "--- big (P7)\n"
		
			+ "- height [num] {cm} (30 250)\n "
			+ "-- < 50\n"
			+ "--- small (P5)\n"
			+ "-- [51 100] \n"
			+ "--- normal (P5)\n"
			+ "-- > 100\n"
			+ "--- big (P7)\n";
		
		String rules = "WENN (BMI < 13 UND weight < 50)\n"
			+ "DANN big = N7";
		
		setUpKB(diagnosis, initQuestion, questions, rules);
		
		System.out.println(writer.writeText());
		System.out.println("TestAbstract -------------------");
		assertTrue(writer.writeText().contains(rules));
		
		rules = "WENN (height > 30 UND weight > 0)\n"
			+ "DANN BMI = (weight / (height * height))";
	
		setUpKB(diagnosis, initQuestion, questions, rules);
		
		System.out.println(writer.writeText());
		System.out.println("TestAbstract -------------------");
		assertTrue(writer.writeText().contains(rules));
	}
	
	private InputStream getStream(String ressource) {
		InputStream stream;
		try {
			stream = new ByteArrayInputStream(ressource.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			stream = null;
		}

		return stream;
	}
	
	private void setUpKB(String diagnosis, String initQuestion, String questions, String rules) {
		initialize();
		
		TextParserResource ressource;

		if (questions != null) {
			ressource = new TextParserResource(getStream(questions));
			input.put(KBTextInterpreter.QU_DEC_TREE, ressource);
		}
		if (diagnosis != null) {
			ressource = new TextParserResource(getStream(diagnosis));
			input.put(KBTextInterpreter.DH_HIERARCHY, ressource);
		}
		if (initQuestion != null) {
			ressource = new TextParserResource(getStream(initQuestion));
			input.put(KBTextInterpreter.QCH_HIERARCHY, ressource);
		}
		if (rules != null) {
			ressource = new TextParserResource(getStream(rules));
			input.put(KBTextInterpreter.COMPL_RULES, ressource);
		}

		output = kbTxtInterpreter.interpreteKBTextReaders(input, "JUnit-KB",
				false, false);
		kb = kbTxtInterpreter.getKnowledgeBase();
		
		setUpWriter();
	}
	
	public void initialize() {
		kbTxtInterpreter = new KBTextInterpreter();
		input = new HashMap<String, TextParserResource>();
		output = new HashMap<String, Report>();
	}
	
	public void setUpWriter() {
		//DataManager.getInstance().setBase(kb);
		manager = new KnowledgeManager(kb);
		manager.setLocale(Locale.GERMAN);
		writer = new RuleWriter(manager);
	}

}
