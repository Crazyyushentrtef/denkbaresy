package de.d3web.we.kdom.rules.action.formula;

import java.util.List;

import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.formula.FormulaNumberElement;
import de.d3web.abstraction.formula.Operator;
import de.d3web.abstraction.formula.QNumWrapper;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.kdom.condition.NonTerminalCondition;
import de.d3web.we.kdom.condition.QuestionNumReference;
import de.d3web.we.kdom.condition.TerminalCondition;
import de.d3web.we.kdom.rules.action.formula.CompositeFormula.CalcMethodType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Factory to create formula according to de.d3web.abstraction.formula format
 * from the Composite-Formula KDOM
 * 
 * 
 * @author Jochen
 * @created 16.10.2010
 */
public class KDOMFormulaNumberElementFactory {

	/**
	 * 
	 * Creates a FormulaNumberElement from some CompositeFormula-Section
	 * (recursively)
	 * 
	 * @created 16.10.2010
	 * @param article
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public static FormulaNumberElement createExpression(Article article, Section<CompositeFormula> c) throws Exception {
		if (c == null) return null;

		// if braced - delegate to next composite
		if (c.get().isBraced(c)) {
			Section<? extends NonTerminalCondition> braced = c.get().getBraced(c);
			return createExpression(article,
						Sections.findSuccessor(braced, CompositeFormula.class));
		}

		// create division
		if (c.get().isDivision(c)) {
			List<Section<? extends CalcMethodType>> divElements = c.get().getDivisionElements(
					c);
			if (divElements.size() != 2) {
				throw new Exception(
						"Houston, we got a Problem:\nTwo operands expected, found: "
								+ divElements.size());
			}

			// build first operand
			Section<? extends CalcMethodType> div0 = divElements.get(0);
			Section<CompositeFormula> compositeChild = Sections.findChildOfType(div0,
					CompositeFormula.class);
			FormulaNumberElement operand0 = createExpression(article, compositeChild);

			// build second operand
			Section<? extends CalcMethodType> div1 = divElements.get(1);
			Section<CompositeFormula> compositeChild1 = Sections.findChildOfType(div1,
					CompositeFormula.class);
			FormulaNumberElement operand1 = createExpression(article, compositeChild1);

			if (operand0 == null || operand1 == null) return null;
			// put together with operation
			Operator op = new Operator(operand0, operand1, Operator.Operation.Div);

			return op;
		}

		// create multiplication
		if (c.get().isMultiplication(c)) {
			List<Section<? extends CalcMethodType>> multElems = c.get().getMultiplicationElements(
					c);

			if (multElems.size() != 2) {
				throw new Exception(
						"Houston, we got a Problem:\nTwo operands expected, found: "
								+ multElems.size());
			}

			// build first operand
			Section<? extends CalcMethodType> mult0 = multElems.get(0);
			Section<CompositeFormula> compositeChild = Sections.findChildOfType(mult0,
					CompositeFormula.class);
			FormulaNumberElement operand0 = createExpression(article, compositeChild);

			// build second operand
			Section<? extends CalcMethodType> mult1 = multElems.get(1);
			Section<CompositeFormula> compositeChild1 = Sections.findChildOfType(mult1,
					CompositeFormula.class);
			FormulaNumberElement operand1 = createExpression(article, compositeChild1);

			if (operand0 == null || operand1 == null) return null;
			// put together with operation
			Operator op = new Operator(operand0, operand1, Operator.Operation.Mult);

			return op;
		}

		// create addition
		if (c.get().isAddition(c)) {
			List<Section<? extends CalcMethodType>> addElems = c.get().getAdditionElements(
					c);

			if (addElems.size() != 2) {
				throw new Exception(
						"Houston, we got a Problem:\nTwo operands expected, found: "
								+ addElems.size());
			}

			// build first operand
			Section<? extends CalcMethodType> add0 = addElems.get(0);
			Section<CompositeFormula> compositeChild = Sections.findChildOfType(add0,
					CompositeFormula.class);
			FormulaNumberElement operand0 = createExpression(article, compositeChild);

			// build second operand
			Section<? extends CalcMethodType> add1 = addElems.get(1);
			Section<CompositeFormula> compositeChild1 = Sections.findChildOfType(add1,
					CompositeFormula.class);
			FormulaNumberElement operand1 = createExpression(article, compositeChild1);

			if (operand0 == null || operand1 == null) return null;
			// put together with operation
			Operator op = new Operator(operand0, operand1, Operator.Operation.Add);

			return op;
		}

		// create subtraction
		if (c.get().isSubtraction(c)) {
			List<Section<? extends CalcMethodType>> subElems = c.get().getSubtractionElements(
					c);

			if (subElems.size() != 2) {
				throw new Exception(
						"Houston, we got a Problem:\nTwo operands expected, found: "
								+ subElems.size());
			}

			// build first operand
			Section<? extends CalcMethodType> sub0 = subElems.get(0);
			Section<CompositeFormula> compositeChild = Sections.findChildOfType(sub0,
					CompositeFormula.class);
			FormulaNumberElement operand0 = createExpression(article, compositeChild);

			// build second operand
			Section<? extends CalcMethodType> sub1 = subElems.get(1);
			Section<CompositeFormula> compositeChild1 = Sections.findChildOfType(sub1,
					CompositeFormula.class);
			FormulaNumberElement operand1 = createExpression(article, compositeChild1);

			if (operand0 == null || operand1 == null) return null;
			// put together with operation
			Operator op = new Operator(operand0, operand1, Operator.Operation.Sub);

			return op;
		}



		// end of recursion => (let) create terminals - eiter numbers of qnums
		if (c.get().isTerminal(c)) {
			Section<? extends TerminalCondition> terminal = c.get().getTerminal(c);

			Section<de.knowwe.core.kdom.basicType.Number> number = Sections.findChildOfType(terminal,
					de.knowwe.core.kdom.basicType.Number.class);
			if (number != null) {
				return new FormulaNumber(
						de.knowwe.core.kdom.basicType.Number.getNumber(number));
			}
			Section<QuestionNumReference> qref = Sections.findChildOfType(terminal,
					QuestionNumReference.class);
			if (qref != null) {
				Question question = qref.get().getTermObject(article, qref);
				if (question instanceof QuestionNum) { // rather has to be
					return new QNumWrapper((QuestionNum) question);
				}
			}


			return null;
		}

		return null;
	}

}
