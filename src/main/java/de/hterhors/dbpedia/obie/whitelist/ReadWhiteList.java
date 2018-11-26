package de.hterhors.dbpedia.obie.whitelist;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import de.hterhors.dbpedia.obie.reader.EasyNTrippleReader;
import de.hterhors.dbpedia.obie.reader.Resource;
import de.hterhors.dbpedia.obie.wikipage.WikiPageReader;
import de.hterhors.dbpedia.obie.wikipage.WikiPageReaderConfig;

public class ReadWhiteList {

	public static void main(String[] args) throws IOException {
		final String tripleFileName = args.length > 0 ? args[0] : "data/ontology_types.nt";// data/universities.nt
		EasyNTrippleReader classesReader = new EasyNTrippleReader(new File(tripleFileName), " ", "#");
		classesReader.read();

		WikiPageReaderConfig wikiPageReaderConfig = new WikiPageReaderConfig(new File("data/en-json"),
				new File("data/en-json/index.tsv"), "\t");

		WikiPageReader.init(wikiPageReaderConfig);
		WikiPageReader wikiPageReader = WikiPageReader.getInstance();

		System.out.println(classesReader.tripples.keySet().size());
		AtomicInteger count = new AtomicInteger();

		Set<Resource> resources = new HashSet<>(classesReader.tripples.keySet());
		classesReader = null;
		System.gc();

		Map<String, Integer> whiteList = new ConcurrentHashMap<>(20_000_000);
		resources.parallelStream().forEach(resource -> {

			if (count.addAndGet(1) % 10_000 == 0)
				System.out.println(count);

			final String[] tokens = wikiPageReader.readWikiPage(resource).text.split("\\W");

			for (int i = 0; i < tokens.length - 1; i++) {

				if (tokens[i].trim().isEmpty() || tokens[i + 1].trim().isEmpty())
					continue;

				final String bigram = tokens[i] + " " + tokens[i + 1];

				whiteList.put(bigram, whiteList.getOrDefault(bigram, 0) + 1);

			}

		});

		List<Map.Entry<String, Integer>> sortedWhiteList = new ArrayList<>(whiteList.size());
		for (Entry<String, Integer> entry : whiteList.entrySet()) {
			sortedWhiteList.add(entry);
		}

		Collections.sort(sortedWhiteList, new Comparator<Map.Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return -Integer.compare(o1.getValue(), o2.getValue());
			}

		});

		final PrintStream ps = new PrintStream(new File("whiteList_bigram"));

		sortedWhiteList.forEach(w -> ps.println(w.getKey() + "\t" + w.getValue()));

		ps.close();
	}

}
