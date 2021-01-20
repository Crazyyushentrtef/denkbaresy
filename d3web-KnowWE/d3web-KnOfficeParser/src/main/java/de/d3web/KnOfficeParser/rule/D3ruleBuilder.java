package de.d3web.KnOfficeParser.rule;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.KnOfficeParser.D3webConditionBuilder;
import de.d3web.KnOfficeParser.DefaultLexer;
import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.D3webQuestionFactory;
import de.d3web.KnOfficeParser.util.DefaultD3webLexerErrorHandler;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.report.Message;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.KnOfficeParser.util.Scorefinder;
import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.Score;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.formula.Add;
import de.d3web.kernel.domainModel.formula.Div;
import de.d3web.kernel.domainModel.formula.FormulaExpression;
import de.d3web.kernel.domainModel.formula.FormulaNumber;
import de.d3web.kernel.domainModel.formula.FormulaNumberElement;
import de.d3web.kernel.domainModel.formula.Mult;
import de.d3web.kernel.domainModel.formula.QNumWrapper;
import de.d3web.kernel.domainModel.formula.Sub;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondDState;

/**
 * Adapterklasse um d3 Regeln zu erstellen
 * 
 * @author Markus Friedrich
 * 
 */
public class D3ruleBuilder implements KnOfficeParser, RuleBuilder {
	private String file;
	private List<Message> errors = new ArrayList<Message>();
	private Question currentquestion;
	private Diagnosis currentdiag;
	private Stack<FormulaNumberElement> formulaStack = new Stack<FormulaNumberElement>();
	private D3webConditionBuilder cb;
	private boolean lazy = false;
	private boolean buildonlywith0Errors = false;
	private int rulecount;
	private List<MyRule> rules = new ArrayList<MyRule>();
	private IDObjectManagement idom;

	private enum ruletype {
		indication, instantindication, contraindication, supress, setvalue, addvalue, heuristic,
	}

	private class MyRule {
		private ruletype type;
		private Question question;
		private AbstractCondition ifcond;
		private AbstractCondition exceptcond;
		private Object[] answers;
		private FormulaExpression formula;
		private ArrayList<QASet> qcons;
		private Score score;
		private Diagnosis diag;

		public MyRule(ruletype type, Question question,
				AbstractCondition ifcond, AbstractCondition exceptcond,
				Object[] answers, FormulaExpression formula,
				ArrayList<QASet> qcons) {
			super();
			this.type = type;
			this.question = question;
			this.ifcond = ifcond;
			this.exceptcond = exceptcond;
			this.answers = answers;
			this.formula = formula;
			this.qcons = qcons;
		}

		public MyRule(Diagnosis diag, Score score, AbstractCondition ifcond,
				AbstractCondition exceptcond) {
			super();
			this.diag = diag;
			this.score = score;
			this.ifcond = ifcond;
			this.exceptcond = exceptcond;
			type = ruletype.heuristic;
		}

	}

	private void addRule(MyRule rule) {
		if (buildonlywith0Errors) {
			rules.add(rule);
		} else {
			generateRule(rule);
		}
	}

	private void generateRule(MyRule rule) {
		String newRuleID = idom.findNewIDFor(new RuleComplex());
		if (rule.type == ruletype.indication) {
			AbstractCondition cond = rule.ifcond;
			if (cond instanceof CondDState) {
				CondDState statecond = (CondDState) cond;
				if (statecond.getStatus() == DiagnosisState.ESTABLISHED) {
					RuleFactory.createRefinementRule(newRuleID, rule.qcons,
							statecond.getDiagnosis(), statecond,
							rule.exceptcond);
				} else if (statecond.getStatus() == DiagnosisState.SUGGESTED) {
					RuleFactory.createClarificationRule(newRuleID, rule.qcons,
							statecond.getDiagnosis(), statecond,
							rule.exceptcond);
				} else {
					RuleFactory.createIndicationRule(newRuleID, rule.qcons,
							cond, rule.exceptcond);
				}
			} else {
				RuleFactory.createIndicationRule(newRuleID, rule.qcons, cond,
						rule.exceptcond);
			}
		} else if (rule.type == ruletype.instantindication) {
			RuleFactory.createInstantIndicationRule(newRuleID, rule.qcons,
					rule.ifcond, rule.exceptcond);
		} else if (rule.type == ruletype.contraindication) {
			RuleFactory.createContraIndicationRule(newRuleID, rule.qcons,
					rule.ifcond, rule.exceptcond);
		} else if (rule.type == ruletype.supress) {
			RuleFactory.createSuppressAnswerRule(newRuleID,
					(QuestionChoice) rule.question, rule.answers, rule.ifcond,
					rule.exceptcond);
		} else if (rule.type == ruletype.setvalue) {
			if (rule.formula != null) {
				RuleFactory.createSetValueRule(newRuleID, rule.question,
						rule.formula, rule.ifcond, rule.exceptcond);
			}else if(rule.answers != null && rule.answers.length > 0) {
				RuleFactory.createSetValueRule(newRuleID, rule.question,
						rule.answers, rule.ifcond, rule.exceptcond);
			}else {
				//TODO add error message
			}
		} else if (rule.type == ruletype.addvalue) {
			if (rule.formula != null) {
				//TODO add factory method so support addValue for formulas
//				RuleFactory.createAddValueRule(newRuleID, rule.question,
//						rule.formula, rule.ifcond, rule.exceptcond);
			}else if(rule.answers != null && rule.answers.length > 0) {
				RuleFactory.createAddValueRule(newRuleID, rule.question,
						rule.answers, rule.ifcond, rule.exceptcond);
			}else {
				//TODO add error message
			}
		} else {
			RuleFactory.createHeuristicPSRule(newRuleID, rule.diag, rule.score,
					rule.ifcond, rule.exceptcond);
		}
		rulecount++;
	}

	private void generateSavedRules() {
		for (MyRule r : rules) {
			generateRule(r);
		}
		rules = new ArrayList<MyRule>();
	}

	private void finish() {
		if (errors.size() == 0) {
			generateSavedRules();
			errors.add(MessageKnOfficeGenerator.createRulesFinishedNote(file,
					rulecount));
		}
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
		cb.setLazy(lazy);
	}

	public boolean isBuildonlywith0Errors() {
		return buildonlywith0Errors;
	}

	public void setBuildonlywith0Errors(boolean buildonlywith0Errors) {
		this.buildonlywith0Errors = buildonlywith0Errors;
	}

	public D3ruleBuilder(String file, boolean lazy, IDObjectManagement idom) {
		this.file = file;
		this.idom = idom;
		cb = new D3webConditionBuilder(file, errors, idom);
		this.lazy = lazy;
		cb.setLazy(lazy);
	}

	public List<Message> getErrors() {
		return errors;
	}

	@Override
	public void indicationrule(int line, String linetext, List<String> names,
			List<String> types, boolean except, boolean instant, boolean not) {
		// Herraussuchen der Frageblätter/Fragen
		ArrayList<QASet> qcons = new ArrayList<QASet>();
		int i = 0;
		String type;
		for (String s : names) {
			type = types.get(i);
			QASet qcon = idom.findQContainer(s);
			if (qcon != null) {
				qcons.add(qcon);
			} else if (!instant) {
				qcon = idom.findQuestion(s);
				if (qcon != null) {
					qcons.add(qcon);
					if (!D3webQuestionFactory.checkType((Question) qcon, type)) {
						errors.add(MessageKnOfficeGenerator
								.createTypeMismatchWarning(file, line,
										linetext, s, type));
					}
				} else {
					if (lazy) {
						if (type != null) {
							qcon = D3webQuestionFactory.createQuestion(s,
									type, idom);
							if (qcon != null) {
								qcons.add(qcon);
							} else {
								errors.add(MessageKnOfficeGenerator
										.createTypeRecognitionError(file, line,
												linetext, s, type));
							}
						} else {
							qcons.add(idom.createQContainer(s, idom
									.getKnowledgeBase().getRootQASet()));
						}
					} else {
						errors
								.add(MessageKnOfficeGenerator
										.createQuestionClassorQuestionNotFoundException(
												file, line, linetext, s));
					}
				}
			} else {
				if (lazy) {
					qcons.add(idom.createQContainer(s, idom.getKnowledgeBase()
							.getRootQASet()));
				} else {
					errors.add(MessageKnOfficeGenerator
							.createQuestionClassNotFoundException(file, line,
									linetext, s));
				}
			}
			i++;
		}
		if (!qcons.isEmpty()) {
			AbstractCondition ifcond;
			AbstractCondition exceptcond;
			if (except) {
				exceptcond = cb.pop();
				ifcond = cb.pop();
				if (exceptcond == null)
					return;
			} else {
				ifcond = cb.pop();
				exceptcond = null;
			}
			if (ifcond == null)
				return;
			ruletype rtype;
			if (!instant && !not) {
				rtype = ruletype.indication;
			} else if (instant) {
				rtype = ruletype.instantindication;
			} else {
				rtype = ruletype.contraindication;
			}
			addRule(new MyRule(rtype, null, ifcond, exceptcond, null, null,
					qcons));
		} else {
			errors.add(MessageKnOfficeGenerator
					.createNoValidQuestionsException(file, line, linetext));
			finishCondstack(except);
		}
	}

	@Override
	public void suppressrule(int line, String linetext, String qname,
			String type, List<String> anames, boolean except) {
		Question q = idom.findQuestion(qname);
		if (!D3webQuestionFactory.checkType(q, type)) {
			errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file,
					line, linetext, qname, type));
		}
		if (q == null) {
			if (lazy) {
				if (type != null) {
					q = D3webQuestionFactory.createQuestion(qname, type, idom);
				} else {
					q = idom.createQuestionOC(qname, idom.getKnowledgeBase()
							.getRootQASet(), new AnswerChoice[0]);
				}
			} else {
				errors.add(MessageKnOfficeGenerator
						.createQuestionNotFoundException(file, line, linetext,
								qname));
				finishCondstack(except);
			}
		} else {
			if (q instanceof QuestionChoice) {
				QuestionChoice qc = (QuestionChoice) q;
				ArrayList<Answer> alist = new ArrayList<Answer>();
				for (String s : anames) {
					Answer a = idom.findAnswer(qc, s);
					if (a != null) {
						alist.add(a);
					} else {
						errors.add(MessageKnOfficeGenerator
								.createAnswerNotFoundException(file, line,
										linetext, s, qc.getText()));
					}
				}
				if (!alist.isEmpty()) {
					AbstractCondition ifcond;
					AbstractCondition exceptcond;
					if (except) {
						exceptcond = cb.pop();
						ifcond = cb.pop();
						if (exceptcond == null)
							return;
					} else {
						ifcond = cb.pop();
						exceptcond = null;
					}
					if (ifcond == null)
						return;
					addRule(new MyRule(ruletype.supress, qc, ifcond,
							exceptcond, alist.toArray(), null, null));
				} else {
					errors
							.add(MessageKnOfficeGenerator
									.createNoValidAnswerException(file, line,
											linetext));
					finishCondstack(except);
				}
			} else {
				errors.add(MessageKnOfficeGenerator.createSupressError(file,
						line, linetext));
				finishCondstack(except);
			}
		}
	}

	@Override
	public void numValue(int line, String linetext, boolean except, String op) {
		if (currentquestion == null) {
			finishCondstack(except);
			return;
		}
		if (currentquestion instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) currentquestion;

			FormulaExpression formula;
			if (op.equals("=")) {
				formula = new FormulaExpression(qnum, formulaStack.pop());
			} else if (op.equals("+=")) {
				FormulaNumberElement fne = new Add(new QNumWrapper(qnum),
						formulaStack.pop());
				formula = new FormulaExpression(qnum, fne);
			} else {
				formula = null;
				errors.add(MessageKnOfficeGenerator
						.createWrongOperatorInAbstractionRule(file, line,
								linetext));
			}
			AbstractCondition ifcond;
			AbstractCondition exceptcond;
			if (except) {
				exceptcond = cb.pop();
				ifcond = cb.pop();
				if (exceptcond == null)
					return;
			} else {
				ifcond = cb.pop();
				exceptcond = null;
			}
			if (ifcond == null)
				return;
			addRule(new MyRule(ruletype.setvalue, qnum, ifcond, exceptcond,
					null, formula, null));
		}
	}

	@Override
	public void questionOrDiagnosis(int line, String linetext, String s,
			String type) {
		currentquestion = idom.findQuestion(s);
		if (currentquestion == null) {
			currentdiag = idom.findDiagnosis(s);
			if (currentdiag == null) {
				if (lazy) {
					if (type != null) {
						currentquestion = D3webQuestionFactory.createQuestion(
								s, type, idom);
					} else {
						currentquestion = D3webQuestionFactory.createQuestion(
								s, "oc", idom);
					}
				} else {
					errors.add(MessageKnOfficeGenerator
							.createQuestionOrDiagnosisNotFoundException(file,
									line, linetext, s));
				}
			}
		} else {
			currentdiag = null;
			if (!D3webQuestionFactory.checkType(currentquestion, type)) {
				errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(
						file, line, linetext, s, type));
			}
			if (!((currentquestion instanceof QuestionNum) || (currentquestion instanceof QuestionChoice))) {
				errors
						.add(MessageKnOfficeGenerator
								.createOnlyNumOrChoiceAllowedError(file, line,
										linetext));
			}
		}
	}

	@Override
	public void choiceOrDiagValue(int line, String linetext, String op,
			String value, boolean except) {
		if (currentquestion == null) {
			if (currentdiag == null) {
				finishCondstack(except);
				return;
			} else {
				if (!op.equals("=")) {
					errors.add(MessageKnOfficeGenerator
							.createWrongOperatorForDiag(file, line, linetext));
					finishCondstack(except);
					return;
				} else {
					Score score = Scorefinder.getScore(value);
					if (score == null) {
						errors.add(MessageKnOfficeGenerator
								.createScoreDoesntExistError(file, line,
										linetext, value));
						finishCondstack(except);
						return;
					} else {
						AbstractCondition ifcond;
						AbstractCondition exceptcond;
						if (except) {
							exceptcond = cb.pop();
							ifcond = cb.pop();
							if (exceptcond == null)
								return;
						} else {
							ifcond = cb.pop();
							exceptcond = null;
						}
						if (ifcond == null)
							return;
						addRule(new MyRule(currentdiag, score, ifcond,
								exceptcond));
					}
				}
			}
		}
		if (currentquestion instanceof QuestionChoice) {
			boolean add = false;
			if (op.equals("=")) {
				add = false;
			} else if (op.equals("+=")) {
				add = true;
			} else {
				errors.add(MessageKnOfficeGenerator
						.createWrongOperatorforChoiceQuestionsException(file,
								line, linetext));
				finishCondstack(except);
				return;
			}
			QuestionChoice qc = (QuestionChoice) currentquestion;
			Answer a = idom.findAnswer(qc, value);
			if (a == null) {
				if (lazy) {
					a = idom.addChoiceAnswer(qc, value);
				} else {
					errors.add(MessageKnOfficeGenerator
							.createAnswerNotFoundException(file, line,
									linetext, value, qc.getText()));
					finishCondstack(except);
					return;
				}
			}
			AbstractCondition ifcond;
			AbstractCondition exceptcond;
			if (except) {
				exceptcond = cb.pop();
				ifcond = cb.pop();
				if (exceptcond == null)
					return;
			} else {
				ifcond = cb.pop();
				exceptcond = null;
			}
			if (ifcond == null)
				return;
			if (add) {
				addRule(new MyRule(ruletype.addvalue, currentquestion, ifcond,
						exceptcond, new Object[] { a }, null, null));

			} else {
				addRule(new MyRule(ruletype.setvalue, currentquestion, ifcond,
						exceptcond, new Object[] { a }, null, null));
			}
		}
	}

	@Override
	public void formula(int line, String linetext, String value) {
		Question q = idom.findQuestion(value);
		FormulaNumberElement num;
		if (q != null) {
			if (q instanceof QuestionNum) {
				QuestionNum qnumValue = (QuestionNum) q;
				num = new QNumWrapper(qnumValue);
			} else {
				num = null;
				errors.add(MessageKnOfficeGenerator
						.createOnlyNumInFormulaError(file, line, linetext));
			}
		} else {
			Double d = null;
			try {
				d = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				if (lazy) {
					q = idom.createQuestionNum(value, idom.getKnowledgeBase()
							.getRootQASet());
				} else {
					errors.add(MessageKnOfficeGenerator
							.createOnlyNumOrDoubleError(file, line, linetext));
					formulaStack.push(null);
					return;
				}
			}
			if (d != null) {
				num = new FormulaNumber(d);
			} else {
				num = new QNumWrapper((QuestionNum) q);
			}
		}
		formulaStack.push(num);
	}

	@Override
	public void formulaAdd() {
		FormulaNumberElement f2 = formulaStack.pop();
		FormulaNumberElement f1 = formulaStack.pop();
		formulaStack.push(new Add(f1, f2));
	}

	@Override
	public void formulaSub() {
		FormulaNumberElement f2 = formulaStack.pop();
		FormulaNumberElement f1 = formulaStack.pop();
		formulaStack.push(new Sub(f1, f2));
	}

	@Override
	public void formulaMult() {
		FormulaNumberElement f2 = formulaStack.pop();
		FormulaNumberElement f1 = formulaStack.pop();
		formulaStack.push(new Mult(f1, f2));
	}

	@Override
	public void formulaDiv() {
		FormulaNumberElement f2 = formulaStack.pop();
		FormulaNumberElement f1 = formulaStack.pop();
		formulaStack.push(new Div(f1, f2));
	}

	@Override
	public List<Message> addKnowledge(Reader r,
			IDObjectManagement idom, KnOfficeParameterSet s) {
		this.idom = idom;
		cb.setIdom(idom);
		ReaderInputStream input = new ReaderInputStream(r);
		ANTLRInputStream istream = null;
		try {
			istream = new ANTLRInputStream(input);
		} catch (IOException e1) {
			errors.add(MessageKnOfficeGenerator.createAntlrInputError(file, 0,
					""));
		}
		DefaultLexer lexer = new DefaultLexer(istream,
				new DefaultD3webLexerErrorHandler(errors, file));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Complexrules parser = new Complexrules(tokens, this,
				new DefaultD3webParserErrorHandler(errors, file, "BasicLexer"),
				cb);
		try {
			parser.knowledge();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		finish();
		return errors;
	}

	@Override
	public Collection<Message> checkKnowledge() {
		finish();
		return errors;
	}

	private void finishCondstack(boolean except) {
		cb.pop();
		if (except)
			cb.pop();
	}
}
