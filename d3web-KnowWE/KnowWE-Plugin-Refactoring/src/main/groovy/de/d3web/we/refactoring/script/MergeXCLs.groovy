package de.d3web.we.refactoring.script

public class MergeXCLs extends RefactoringScriptGroovy{

	@Override
	public void run() {
		type = getTypeFromString('KnowWEArticle')
		objectID = findObjectID(type)
		coveringListContent = findCoveringListContent(objectID)
		xcls = findXCLs(coveringListContent)
		map = [:]
		xcls.each{
			solutionName = findSolutionID(it)
			set = map.get(solutionName)
			set = (set != null)? set : new TreeSet()
			map.put(solutionName,set)
			findFindings(it).each{
				set.add(it.getOriginalText())
			}
			//deleteXCL it
		}
		setMergedCoveringListContent coveringListContent, map
	}
}