package benchmarks;

import java.util.ArrayList;
import java.util.List;

public class ArticleBenchmarkGenerator {
	
	public static void main(String[] args) {
		
		List<KnowledgeModule> modules = new ArrayList<KnowledgeModule>();
		modules.add(new QuestionTreeModule());
		
		for (KnowledgeModule module:modules) {
			System.out.println(module.generateModuleText(50, 0));
		}
		
	}
	
	

}
