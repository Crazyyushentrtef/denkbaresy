package de.d3web.we.refactoring.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.content.PageRenamer;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.basic.CommentLineType;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.SubTree;
import de.d3web.we.kdom.dashTree.questionnaires.QuestionnairesSection;
import de.d3web.we.kdom.dashTree.solutions.SolutionDef;
import de.d3web.we.kdom.dashTree.solutions.SolutionsSection;
import de.d3web.we.kdom.decisionTree.QClassID;
import de.d3web.we.kdom.decisionTree.QuestionsSection;
import de.d3web.we.kdom.decisionTree.QuestionsSectionContent;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.defaultMarkup.ContentType;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.objects.QuestionID;
import de.d3web.we.kdom.objects.QuestionTreeAnswerID;
import de.d3web.we.kdom.objects.QuestionnaireID;
import de.d3web.we.kdom.questionTreeNew.QuestionTreeRootType;
import de.d3web.we.kdom.questionTreeNew.SetValueLine;
import de.d3web.we.kdom.rules.RulesSection;
import de.d3web.we.kdom.rules.RulesSectionContent;
import de.d3web.we.kdom.table.attributes.AttributeTableSection;
import de.d3web.we.kdom.xcl.CoveringListContent;
import de.d3web.we.kdom.xcl.CoveringListSection;
import de.d3web.we.kdom.xcl.XCList;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.refactoring.management.RefactoringManager;

public abstract class RefactoringScript {
	protected final Collection<Class<? extends KnowWEObjectType>> KNOWLEDGE_MAIN_SECTIONS;
	private RefactoringSession refactoringSession;
	private RefactoringManager refManager(){
		return refactoringSession.refManager;
	}
	private Map<String, String[]> gsonFormMap() {
		return refactoringSession.gsonFormMap;
	}
	private WikiEngine we(){
		return refactoringSession.we;
	}
	
	public RefactoringScript() {
		Set<Class<? extends KnowWEObjectType>> kms = new HashSet<Class<? extends KnowWEObjectType>>();
		kms.add(RulesSection.class);
		kms.add(CoveringListSection.class);
		kms.add(QuestionTreeRootType.class);
		kms.add(QuestionsSection.class);
		kms.add(AttributeTableSection.class);
		kms.add(SolutionsSection.class);
		kms.add(QuestionnairesSection.class);
		KNOWLEDGE_MAIN_SECTIONS = Collections.unmodifiableCollection(kms);
	}
	
	public void setSession(RefactoringSession refactoringSession) {
		this.refactoringSession = refactoringSession;
	}
	
	private void performNextAction(DeprecatedAbstractKnowWEAction a) {
		refactoringSession.performNextAction(a);
	}
	
	public abstract void run();

	public Section<?> findXCList() {
		performNextAction(new DeprecatedAbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie die zu transformierende Überdeckungsliste aus:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>XCList</label>"
						+ "<select name='selectXCList' class='refactoring'>");
				for(Iterator<KnowWEArticle> it = refManager().getArticleIterator(); it.hasNext();) {
					KnowWEArticle article = refManager().getArticle(it.next().getTitle());
					Section<?> articleSection = article.getSection();
					List<Section<XCList>> xclists = new ArrayList<Section<XCList>>();
					articleSection.findSuccessorsOfType(XCList.class, xclists);
					for (Section<?> xclist : xclists) {
						html.append("<option value='" + xclist.getId() + "'>Seite: " + article.getTitle() + " - XCList: " 
								+ xclist.findSuccessor(SolutionID.class).getOriginalText() + "</option>");
					}
				}
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		Section<?> knowledge = refManager().
					findNode(gsonFormMap().
					get("selectXCList")[0]);
		return knowledge;
	}

	public Class<? extends KnowWEObjectType> findRenamingType() {
		performNextAction(new DeprecatedAbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den Typ des Objekts aus, welches sie umbenennen möchten:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objekttyp</label>"
						+ "<select name='selectRenamingType' class='refactoring'>");
				html.append("<option value='KnowWEArticle'>KnowWEArticle (Wikiseite)</option>");
				html.append("<option value='QuestionnaireID'>QuestionnaireID (Fragebogen)</option>");
				html.append("<option value='QuestionID'>QuestionID (Frage)</option>");
				html.append("<option value='QuestionTreeAnswerID'>QuestionTreeAnswerID (Antwort)</option>");
				html.append("<option value='SolutionID'>SolutionID (Lösung)</option>");
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		String clazzString = gsonFormMap().get("selectRenamingType")[0];
		Class<? extends KnowWEObjectType> clazz = getTypeFromString(clazzString);
		return clazz;
	}

	public <T extends KnowWEObjectType> String findObjectID(final Class<T> clazz) {
		//TODO mehrfache Einträge für ein Element sollten vermieden werden
		performNextAction(new DeprecatedAbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den Namen des Objekts mit dem Typ <strong>" + clazz.getName() + "</strong>:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objektname</label>"
						+ "<select name='selectObjectID' class='refactoring'>");
				for(Iterator<KnowWEArticle> it = refManager().getArticleIterator(); it.hasNext();) {
					KnowWEArticle article = refManager().getArticle(it.next().getTitle());
					Section<?> articleSection = article.getSection();
					List<Section<T>> objects = new ArrayList<Section<T>>();
					articleSection.findSuccessorsOfType(clazz, objects);
					for (Section<?> object : objects) {
						String name = (clazz == KnowWEArticle.class) ? object.getId() : object.getOriginalText();
						String question = (clazz == QuestionTreeAnswerID.class)
							? " - Frage: " + findDashTreeFather(object, QuestionID.class).getOriginalText()
							: "";
						html.append("<option value='" + object.getId() + "'>Seite: " + article.getTitle() + question + " - Objekt: " 
								+ name + "</option>");
					}
				}
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		String objectID = gsonFormMap().get("selectObjectID")[0];
		return objectID;
	}

	public <T extends KnowWEObjectType> String[] findObjectIDs(final Class<T> clazz) {
		//TODO mehrfache Einträge für ein Element sollten vermieden werden
		performNextAction(new DeprecatedAbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den Namen des Objekts mit dem Typ <strong>" + clazz.getName() + "</strong>:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objektname</label>"
						+ "<select multiple size='" + refManager().getArticles().size() + "' name='selectObjectID' class='refactoring'>");
				for(Iterator<KnowWEArticle> it = refManager().getArticleIterator(); it.hasNext();) {
					KnowWEArticle article = refManager().getArticle(it.next().getTitle());
					Section<?> articleSection = article.getSection();
					List<Section<T>> objects = new ArrayList<Section<T>>();
					articleSection.findSuccessorsOfType(clazz, objects);
					for (Section<?> object : objects) {
						String name = (clazz == KnowWEArticle.class) ? object.getId() : object.getOriginalText();
						String question = (clazz == QuestionTreeAnswerID.class)
							? " - Frage: " + findDashTreeFather(object, QuestionID.class).getOriginalText()
							: "";
						html.append("<option value='" + object.getId() + "'>Seite: " + article.getTitle() + question + " - Objekt: " 
								+ name + "</option>");
					}
				}
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		String[] objectIDs = gsonFormMap().get("selectObjectID");
		return objectIDs;
	}

	public String findNewName() {
		performNextAction(new DeprecatedAbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den neuen Namen des gewählten Objekts:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objektname</label>"
						+ "<input type='text' name='selectNewName' class='refactoring'>");
				html.append("</div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		String newName = gsonFormMap().get("selectNewName")[0];
		return newName.trim();
	}

	public void printExistingElements(final List<Section<? extends KnowWEObjectType>> existingElements) {
		performNextAction(new DeprecatedAbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				StringBuffer html = new StringBuffer();
				html.append("<br />Das Refactoring kann nicht durchgeführt werden, da es Konflikte mit folgenden Sektionen gibt:<br /><br />");
				for (Section<? extends KnowWEObjectType> section: existingElements) {
					html.append("<br />ID: " + section.getId() + "<br />");
					html.append("Inhalt: " + section.getOriginalText() + "<br />");
				}
				html.append("<fieldset><form name='refactoringForm'><div>"
						+ "<input type='button' value='Abbrechen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
	}

	public void addRulesText(StringBuffer sb, Section<RulesSectionContent> rulesSectionContent) {
		replaceSection(rulesSectionContent, rulesSectionContent.getOriginalText() + sb.toString());
	}

	public Section<CoveringListContent> findCoveringListContent(String articleID) {
		//FIXME es soll keine neue erzeugt werden, wenn keine vorhanden
		return findCoveringListContent(refManager().getArticle(articleID).getSection());
	}

	public List<Section<XCList>> findXCLs(Section<CoveringListContent> content) {
		List<Section<XCList>> list = new LinkedList<Section<XCList>>();
		content.findSuccessorsOfType(XCList.class, list);
		return list;
	}

	public void setMergedCoveringListContent(Section<CoveringListContent> section, Map<String,Set<String>> map) {
		StringBuffer sb = new StringBuffer();
		for(String s: map.keySet()) {
			sb.append("\n" + s +"{\n");
			for(String finding: map.get(s)) {
				sb.append("\t" + finding + ",\n");
			}
			sb.append("}");
		}
		replaceSection(section, sb.toString());
	}

	public Section<RulesSectionContent> findRulesSectionContent(Section<?> knowledgeSection) {
		// FIXME temporärer hack reloaded
		KnowWEArticle article = refManager().getArticle(knowledgeSection.getArticle().getTitle());
		
		Section<RulesSectionContent> rulesSectionContent = article.getSection().findSuccessor(RulesSectionContent.class);
		if (rulesSectionContent == null) {
			// TODO ancestoroftype
			Section<? extends KnowWEObjectType> anc = knowledgeSection.findAncestor(KNOWLEDGE_MAIN_SECTIONS);
			if (anc == null) {
				anc = article.getSection();
			} else {
				// FIXME temporärer hack reloaded
				anc = refManager().findNode(anc.getId());
			}
			StringBuilder newArticleText = new StringBuilder(anc.getOriginalText());
			// Artikel wird gespeichert um an die RulesSection ranzukommen
			refManager().replaceKDOMNode(anc.getArticle().getTitle(), anc.getId(), newArticleText.append("\r\n<Rules-section>\r\n" + 
					"\r\n" + 
					"</Rules-section>\r\n").toString());
			Section<RulesSectionContent> rulesSectionContentNew = refManager().getArticle(article.getTitle())
				.getSection().findSuccessor(RulesSectionContent.class);
			return rulesSectionContentNew;
		}
		return rulesSectionContent;
	}

	public Section<CoveringListContent> findCoveringListContent(Section<?> knowledgeSection) {
		// FIXME temporärer hack reloaded		
		KnowWEArticle article = refManager().getArticle(knowledgeSection.getArticle().getTitle());
		
		Section<CoveringListContent> coveringListContent = article.getSection().findSuccessor(CoveringListContent.class);
		if (coveringListContent == null) {
			// TODO ancestoroftype
			Section<? extends KnowWEObjectType> anc = knowledgeSection.findAncestor(KNOWLEDGE_MAIN_SECTIONS);
			if (anc == null) {
				anc = article.getSection();
			} else {
				// FIXME temporärer hack reloaded
				anc = refManager().findNode(anc.getId());
			}
			StringBuilder newArticleText = new StringBuilder(anc.getOriginalText());
			// Artikel wird gespeichert um an die CoveringListSection ranzukommen
			refManager().replaceKDOMNode(anc.getArticle().getTitle(), anc.getId(), newArticleText.append("\r\n<SetCoveringList-section>\r\n" + 
					"\r\n" + 
					"</SetCoveringList-section>\r\n").toString());
			Section<CoveringListContent> coveringListContentNew = refManager().getArticle(article.getTitle())
				.getSection().findSuccessor(CoveringListContent.class);
			return coveringListContentNew;
		}
		return coveringListContent;
	}

	public void deleteXCList(Section<?> knowledgeSection) {
		replaceSection(knowledgeSection, "\n");
		// FIXME hack
		refManager().saveUpdatedArticle(refManager().getArticle(knowledgeSection.getArticle().getTitle()));
	}

	public void createRulesText(Section<Finding> sec, String solutionID, StringBuffer sb) {
		sb.append("\nIF " + sec.getOriginalText());
		sb.append("\n    THEN ");
		sb.append(solutionID + " = P7");
	}

	public String findSolutionID(Section<?> knowledgeSection) {
		Section<SolutionID> solutionID = knowledgeSection.findSuccessor(SolutionID.class);
		return solutionID.getOriginalText();
	}

	public List<Section<Finding>> findFindings(Section<?> knowledgeSection) {
		List<Section<Finding>> findings = new ArrayList<Section<Finding>>();
		knowledgeSection.findSuccessorsOfType(Finding.class, findings);
		return findings;
	}

	public Class<? extends KnowWEObjectType> getTypeFromString(String clazzString) {
		Class<? extends KnowWEObjectType> clazz = null;
		if(clazzString.equals("KnowWEArticle")) {
			clazz = KnowWEArticle.class;
		} else if(clazzString.equals("QuestionnaireID")) {
			clazz = QuestionnaireID.class;
		} else if(clazzString.equals("QuestionID")) {
			clazz = QuestionID.class;
		} else if(clazzString.equals("QuestionTreeAnswerID")) {
			clazz = QuestionTreeAnswerID.class;
		} else if(clazzString.equals("SolutionID")) {
			clazz = SolutionID.class;
		}
		return clazz;
	}

	protected <T extends KnowWEObjectType> Section<T> findDashTreeFather(Section<?> section, Class<T> clazz) {
		return section
			.findAncestor(SubTree.class).findAncestor(SubTree.class)
			.findChildOfType(DashTreeElement.class).findSuccessor(clazz);
	}

	public <T extends KnowWEObjectType> List<Section<? extends KnowWEObjectType>> findRenamingList(Class<T> clazz, String objectID) {
		return findRenamingList(clazz, objectID, null);
	}

	public <T extends KnowWEObjectType> List<Section<? extends KnowWEObjectType>> findRenamingList(Class<T> clazz, String objectID, String newName) {
		List<Section<? extends KnowWEObjectType>> fullList = new ArrayList<Section<? extends KnowWEObjectType>>();
		List<Section<? extends KnowWEObjectType>> filteredList = new ArrayList<Section<? extends KnowWEObjectType>>();
		if (clazz == QuestionTreeAnswerID.class) {
			Section<QuestionID> question = findDashTreeFather(refManager().findNode(objectID), QuestionID.class);
	
			// TODO: diesen kommentar anpassen :-)/ hole alle FindingQuestion's welche den gleichen getOriginalText() haben wie die Question, zu welcher die QuestionTreeAnswerID
			// gehört
			List<Section<? extends KnowWEObjectType>> questions = findRenamingList(QuestionID.class, question.getId());
			// bestimme dafür die passenden Antworten
			for (Section<? extends KnowWEObjectType> questionSection : questions){ 
				if (questionSection.get().getClass() == FindingQuestion.class) {
						Section<? extends KnowWEObjectType> answer = questionSection.getFather().findSuccessor(FindingAnswer.class);
						fullList.add(answer);
				}
				if (questionSection.get().getClass() == QuestionID.class) {
					List<Section<QuestionTreeAnswerID>> answers = new LinkedList<Section<QuestionTreeAnswerID>>();
					questionSection.findAncestor(SubTree.class).findSuccessorsOfType(QuestionTreeAnswerID.class, 5 , answers);
					fullList.addAll(answers);
					
					if(questionSection.getFather().get().getClass() == SetValueLine.class) {
						fullList.add(questionSection.getFather().findChildOfType(AnonymousType.class));
					}
				};
			}
		} else {
			for (Iterator<KnowWEArticle> it = refManager().getArticleIterator(); it.hasNext();) {
				KnowWEArticle article = refManager().getArticle(it.next().getTitle());
				Section<?> articleSection = article.getSection();
				List<Section<T>> objects = new ArrayList<Section<T>>();
				articleSection.findSuccessorsOfType(clazz, objects);
				fullList.addAll(objects);
				if (clazz == QuestionnaireID.class || clazz == QuestionID.class) {
					List<Section<QClassID>> objects2 = new ArrayList<Section<QClassID>>();
					articleSection.findSuccessorsOfType(QClassID.class, objects2);
					fullList.addAll(objects2);
				}
				if (clazz == QuestionID.class) {
					List<Section<FindingQuestion>> objects2 = new ArrayList<Section<FindingQuestion>>();
					articleSection.findSuccessorsOfType(FindingQuestion.class, objects2);
					fullList.addAll(objects2);
				}
				if (clazz == SolutionID.class) {
					List<Section<QuestionID>> objects2 = new ArrayList<Section<QuestionID>>();
					articleSection.findSuccessorsOfType(QuestionID.class, objects2);
					fullList.addAll(objects2);
					List<Section<SolutionDef>> objects3 = new ArrayList<Section<SolutionDef>>();
					articleSection.findSuccessorsOfType(SolutionDef.class, objects3);
					fullList.addAll(objects3);
					List<Section<FindingQuestion>> objects4 = new ArrayList<Section<FindingQuestion>>();
					articleSection.findSuccessorsOfType(FindingQuestion.class, objects4);
					fullList.addAll(objects4);
				}
			}
		}
		for (Section<? extends KnowWEObjectType> object : fullList) {
			String name = (newName != null) ? newName : refManager().findNode(objectID).getOriginalText().trim();
			if (object.getOriginalText().trim().equals(name)) {
				filteredList.add(object);
			}
			if (object.get().getClass() == AnonymousType.class) {
				String oText = object.getOriginalText().trim();
				if (oText.substring(1, oText.length()-1).trim().equals(name)) {
					filteredList.add(object);
				}
			}
		}
		return filteredList;
	}

	public <T extends KnowWEObjectType> void renameElement(Section<? extends KnowWEObjectType> section, String newName, Class<T> clazz) {
			// TODO Report und Undo von umbenannten Wiki-Seiten ermöglichen
			if (clazz == KnowWEArticle.class) {
				PageRenamer pr = new PageRenamer();
				try {
					WikiPage page = we().getPage(section.getId());
					WikiContext wc = new WikiContext(we(), page);
					pr.renamePage(wc, section.getId(), newName, true);
					//TODO wenn man die WikiEngine vermeiden möchte - Nachteil: Wiki-Links werden nicht mit umbenannt. Vorteil: man spart sich die
					//NullpointerException wenn man den Test hierfür ausführt
	//				KnowWEArticle oldArt = section.getArticle();
	//				KnowWEArticle newArt = new KnowWEArticle(oldArt.toString(), newName, oldArt.getAllowedChildrenTypes(), oldArt.getWeb());
	//				refManager.saveUpdatedArticle(newArt);
				} catch (Exception e) {
					// TODO wird z.B. geworfen wenn der Seitenname bereits vorhanden ist oder wenn der Test hier vorbeikommt und nicht auf
					// die WikiEngine zugreifen kann.
					e.printStackTrace();
				}
				// include-Referenzen werden umbenannt
				for (Iterator<KnowWEArticle> it = refManager().getArticleIterator(); it.hasNext();) {
					KnowWEArticle art = it.next();
					List<Section<Include>> includes = new LinkedList<Section<Include>>();
					art.getSection().findSuccessorsOfType(Include.class, includes);
					for(Section<Include> inc : includes) {
						Map<String,String> attributes = AbstractXMLObjectType.getAttributeMapFor(inc);
						String src = attributes.get("src");
						String articleName = refManager().getArticleName(src);
						if (articleName.equals(section.getArticle().getTitle())) {
							String newSrc = src.replaceFirst(articleName, newName);
							StringBuffer replacement = new StringBuffer("<include src=\"" + newSrc + "\" />");
							replaceSection(inc, replacement.toString());
						}
					}
				}
			} else if (section.get().getClass() == AnonymousType.class){
				replaceSection(section, "(" + newName + ")");
			}
			else {
				replaceSection(section, newName);
			}
		}

	public List<Section<QuestionID>> findSolutions(String value, String pageName) {
		List<Section<QuestionID>> returnList = new LinkedList<Section<QuestionID>>();
		Section<?> articleSection = refManager().findNode(pageName);
		List<Section<AnonymousType>> found = new ArrayList<Section<AnonymousType>>();
		// TODO es muss noch überprüft werden, ob AnonymousType.getName().equals("SetValueArgument") gilt.
		articleSection.findSuccessorsOfType(AnonymousType.class, found);
		for (Section<AnonymousType> atSection: found) {
			if (atSection.getOriginalText().equals(value)) {
				Section<QuestionID> foundSolution = atSection.getFather().findSuccessor(QuestionID.class);
				returnList.add(foundSolution);
			}
		}
		return returnList;
	}

	public void createXCLFromFindingsTrace(Section<QuestionID> solution) {
		Section<QuestionTreeAnswerID> answer = findDashTreeFather(solution, QuestionTreeAnswerID.class);
		Section<QuestionID> question = findDashTreeFather(answer, QuestionID.class);
		StringBuffer sb = new StringBuffer();
		sb.append("\n" + solution.getOriginalText() +"{\n");
		traceFindings(sb, question, answer);
		sb.append("}");
		Section<CoveringListContent> covCon = findCoveringListContent(solution);
		replaceSection(covCon, covCon.getOriginalText() + sb.toString());
	}

	private void traceFindings(StringBuffer sb, Section<QuestionID> question, Section<QuestionTreeAnswerID> answer) {
		sb.append("\t" + question.getOriginalText() + " = " + answer.getOriginalText() + ",\n");
		Section<QuestionTreeAnswerID> nextAnswer = findDashTreeFather(question, QuestionTreeAnswerID.class);
		if(nextAnswer != null) {
			Section<QuestionID> nextQuestion = findDashTreeFather(nextAnswer, QuestionID.class);
			traceFindings(sb, nextQuestion, nextAnswer);
		}
	}

	public void deleteSolutionOccurrences(Section<QuestionID> solution) {
		// FIXME temporärer hack reloaded
		KnowWEArticle article = refManager().getArticle(solution.getArticle().getTitle());
		
		List<Section<QuestionID>> list = new LinkedList<Section<QuestionID>>();
		article.getSection().findSuccessorsOfType(QuestionID.class, list);
		for(Section<QuestionID> sqid: list) {
			if (sqid.getOriginalText().equals(solution.getOriginalText())) {
				replaceSection(sqid.findAncestor(SubTree.class).findChildOfType(DashTreeElement.class), "");
			}
		}
	}

	public void deleteComments(String sectionID) {
		Section<?> section = refManager().findNode(sectionID);
		List<Section<CommentLineType>> list = new LinkedList<Section<CommentLineType>>();
		section.getArticle().getSection().findSuccessorsOfType(CommentLineType.class, list);
		for(Section<CommentLineType> commentLine: list) {
			replaceSection(commentLine, "");
		}
	}

	public void transformToQuestionTree(String objectID) {
		Section<?> section = refManager().findNode(objectID);
		Section<QuestionsSection> qs = section.findSuccessor(QuestionsSection.class);
		Section<QuestionsSectionContent> qsc = qs.findSuccessor(QuestionsSectionContent.class);
		replaceSection(qs,"\n%%QuestionTree\n" + qsc.getOriginalText() + "\n%\n"); 
	}

	public void transformToQuestionsSection(String objectID) {
		Section<?> section = refManager().findNode(objectID);
		Section<QuestionTreeRootType> qtrt = section.findSuccessor(QuestionTreeRootType.class);
		Section<ContentType> ct = qtrt.findSuccessor(ContentType.class);
		replaceSection(qtrt,"\n<Questions-section>\n" + ct.getOriginalText() + "\n</Questions-section>\n"); 
	}

	private void replaceSection(Section<?> section, String newText) {
		KnowWEArticle article = section.getArticle(); 
		String topic = article.getTitle();
		refManager().replaceKDOMNodeWithoutSave(topic, section.getId(), newText);
	}



}
