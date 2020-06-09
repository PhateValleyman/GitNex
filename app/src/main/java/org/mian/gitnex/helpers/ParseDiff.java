package org.mian.gitnex.helpers;

import org.mian.gitnex.models.FileDiffView;
import java.util.ArrayList;
import java.util.List;

/**
 * Author 6543
 */

public class ParseDiff {

	private static String[] getFileNames(String raw) {

		String[] lines2 = raw.split(" b/");
		if(lines2.length < 2) {
			return new String[1];
		}
		String oldName = lines2[0];
		String newName = lines2[1].split("\\n")[0];
		return new String[]{oldName, newName};
	}

	private static String getFileInfo(String raw) {

		if(raw.contains("\ndeleted file mode \\d+\n")) {
			return "delete";
		}
		else if(raw.contains("\nnew file mode \\d+\n")) {
			return "new";
		}
		return "change";
	}

	public static List<FileDiffView> getFileDiffViewArray(String raw) {

		List<FileDiffView> fileContentsArray = new ArrayList<>();

		String[] lines = raw.split("(^|\\n)diff --git a/");
		if(lines.length > 1) {

			// for each file in diff
			for(int i = 1; i < lines.length; i++) {

				// check if it is a binary file
				if(lines[i].contains("\nBinary files a/")) {
					String[] fileNames = getFileNames(lines[i]);
					if(fileNames.length != 2) {
						continue;
					}
					fileContentsArray.add(new FileDiffView(fileNames[0], fileNames[1], "binary", "", null));
				}


				// check if it is a binary patch
				else if(lines[i].contains("\nGIT binary patch\n")) {
					String[] fileNames = getFileNames(lines[i]);
					if(fileNames.length != 2) {
						continue;
					}

					String[] tmp = lines[i].split("literal \\d+\\n");
					String rawContent = "";
					if(tmp.length >= 2) {
						rawContent = tmp[1].replace("\n", "");
					}

					List<FileDiffView.Content> contents = new ArrayList<>();
					contents.add(new FileDiffView.Content(rawContent));
					fileContentsArray.add(new FileDiffView(fileNames[0], fileNames[1], "binary", getFileInfo(lines[i]), contents));
				}


				// check if it is normal diff
				else if(lines[i].contains("\n@@ -")) {
					String[] fileNames = getFileNames(lines[i]);
					if(fileNames.length != 2) {
						continue;
					}
					String[] rawDiffs = lines[i].split("\n@@ -");
					if(rawDiffs.length <= 1) {
						continue;
					}
					List<FileDiffView.Content> contents = new ArrayList<>();
					for(int j = 1; j < rawDiffs.length; j++) {
						String[] rawDiff = rawDiffs[j].split(" @@\n");
						if(rawDiff.length <= 1) {
							continue;
						}

						int oldStart = 0, newStart = 0, added = 0, removed = 0;
						String stats[] = rawDiff[0].split(" \\+");
						if(stats.length == 2) {
							String aStats[] = stats[0].split(",");
							if(aStats.length >= 1) {
								oldStart = Integer.parseInt(aStats[0]);
								removed = oldStart;
							}
							if(aStats.length == 2) {
								removed = Integer.parseInt(aStats[1]);
							}
							String bStats[] = stats[1].split(",");
							if(bStats.length >= 1) {
								newStart = Integer.parseInt(bStats[0]);
								added = newStart;
							}
							if(bStats.length == 2) {
								added = Integer.parseInt(bStats[1]);
							}
						}


						contents.add(new FileDiffView.Content(rawDiff[1], oldStart, newStart, added, removed));
					}
					fileContentsArray.add(new FileDiffView(fileNames[0], fileNames[1], "diff", getFileInfo(lines[i]), contents));
				}


				// a rename
				else if(lines[i].contains("\nrename from")) {
					String[] lines2 = lines[i].split("\\nrename (from|to )");
					if(lines2.length != 3) {
						continue;
					}
					fileContentsArray.add(new FileDiffView(lines2[1], lines2[2].split("\\n")[0], "rename", "rename", null));
				}
			}
		}

		return fileContentsArray;

	}

}
