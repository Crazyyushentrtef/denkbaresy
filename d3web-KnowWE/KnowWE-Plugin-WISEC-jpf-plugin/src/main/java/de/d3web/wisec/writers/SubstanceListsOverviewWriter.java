package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class SubstanceListsOverviewWriter extends WISECWriter {

	public static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "AllSubstanceLists";
	
	public final static String[] LIST_ATTRIBUTES = new String[] {
			"Source_ID", "ID", "Name", "Criteria_Code", "List_allocation", "Number_of_substances",
			"CMR", "Persistence", "Bioakumulation_Potential", "Aqua_Tox", "PBT", "vPvB", "EDC",
			"Multiple_Tox", "LRT", "Climatic_Change", "drinking_water", " surface_water", "sea",
			"groundwater", "Risk_related", "Exposure", "compartment", "Market_Volume",
			"Wide_d_use", "Political", "SVHC_regulated", "Regulated", "ecological_concerns" };
	
	private final static String[] WRITEABLE_ATTR = new String[] {
			"Source_ID", "Source_Name", "ID", "Name", "Author", "Country"
	};
	
	public SubstanceListsOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory+FILENAME+".txt");
		writeBreadcrumb(writer);
		writeHeader(writer);

		List<SubstanceList> sortedSubstances = new ArrayList<SubstanceList>(
				model.getSubstanceLists());
		Collections.sort(sortedSubstances, new Comparator<SubstanceList>() {
			@Override
			public int compare(SubstanceList o1, SubstanceList o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});

		for (SubstanceList list : sortedSubstances) {
			SourceList sourceList = model.getSourceListForID(list.info.get("Source_ID"));

			StringBuffer buffy = new StringBuffer();
			

			buffy.append("| " + getWikiedValueForAttribute("Source_ID", list) + " ");
			buffy.append("| [" + sourceList.get("Name") + " | " +
					SourceListWriter.getWikiFilename(sourceList.getId()) + "] ");
			buffy.append("| " + getWikiedValueForAttribute("ID", list) + " ");
			buffy.append("| " + getWikiedValueForAttribute("Name", list) + " ");
			buffy.append("| " + sourceList.get("Author") + " ");
			buffy.append("| " + sourceList.get("Country") + " ");
			buffy.append("\n");
			writer.write(buffy.toString());

		}
		
		writeFooter(writer);
		writer.close();

	}

	@Override
	protected void writeBreadcrumb(Writer writer) throws IOException {
		super.writeBreadcrumb(writer);
		writer.append(" > Index of Lists\n\n");
	}

	
	public static String generateOverviewLineFor(SubstanceList list, String[] listAttr) {
		if (listAttr == null) {
			listAttr = LIST_ATTRIBUTES;
		}
		String filename = SubstanceListWriter.getWikiFileNameFor(list.getId());
		StringBuffer buffy = new StringBuffer();
		for (String attribute : listAttr) {
			if (attribute.equalsIgnoreCase("Name")) {
				buffy.append("| [" + clean(list.getName()) + " | " + filename + "] "); // Name
																					// of
																					// the
																					// List
			}
			else {
				String value = list.info.get(attribute);
				if (value == null) {
					value = "";
				}
				buffy.append("| " + clean(value) + " ");
			}
		}

		return buffy.toString();
	}

	private String getWikiedValueForAttribute(String attribute, SubstanceList list) {
		StringBuffer buffy = new StringBuffer();
		if (attribute.equalsIgnoreCase("Name")) {
			buffy.append("[" + clean(list.getName()) + " | "
					+ SubstanceListWriter.getWikiFileNameFor(list.getId()) + "] ");
		}
		else {
			String value = list.info.get(attribute);
			if (value == null) {
				value = "";
			}
			buffy.append(clean(value) + " ");
		}
		return buffy.toString();
	}

	private static String clean(String string) {
		return ConverterUtils.clean(string);
	}

	
	private void writeFooter(Writer writer) throws IOException {
		// close the zebra and the sortable table
		writer.append("/%\n/%\n");
	}

	private void writeHeader(Writer writer) throws IOException {
		writer.append("!!! Lists\n\n");
		// open the zebra and the sortable table
		writer.append("%%zebra-table\n%%sortable\n");
		// write all header names
		writer.append(writeTableHeader() + "\n");
		// writer.append("|| No || Upper List || List || Used || Unused || Criteria \n");
	}

	public static String writeTableHeader() {
		StringBuffer b = new StringBuffer();
		for (String header : WRITEABLE_ATTR) {
			b.append("|| " + header + " ");
		}
		return b.toString();
	}
}
