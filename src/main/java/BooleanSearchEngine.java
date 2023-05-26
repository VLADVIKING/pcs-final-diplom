import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> wordUnique = new HashMap<>();
    private Set<String> stopWords = new HashSet<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {

        File[] files = pdfsDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            var doc = new PdfDocument(new PdfReader(String.valueOf(new File(pdfsDir, files[i].getName()))));
            for (int j = 1; j <= doc.getNumberOfPages(); j++) {
                var text = PdfTextExtractor.getTextFromPage(doc.getPage(j));
                var words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>();
                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }
                for (Map.Entry<String, Integer> item : freqs.entrySet()) {
                    if (wordUnique.containsKey(item.getKey())) {
                        List<PageEntry> pageEntries = new ArrayList<>();
                        pageEntries.addAll(wordUnique.get(item.getKey()));
                        pageEntries.add(new PageEntry(files[i].getName(), j, item.getValue()));
                        Collections.sort(pageEntries, PageEntry::compareTo);
                        wordUnique.replace(item.getKey(), pageEntries);
                        continue;
                    }
                    List<PageEntry> pageEntries = new ArrayList<>();
                    pageEntries.add(new PageEntry(files[i].getName(), j, item.getValue()));
                    wordUnique.put(item.getKey(), pageEntries);
                }
            }
        }
        loadStopTxt();
    }

    @Override
    public List<PageEntry> search(String text) {
        String[] words = text.split("\\P{IsAlphabetic}+");
        List<PageEntry> searchResult = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            if (!stopWords.contains(words[i].toLowerCase())) {
                if (wordUnique.containsKey(words[i].toLowerCase())) {
                    searchResult.addAll(wordUnique.get(words[i].toLowerCase()));
                }
            }
        }
        Map<String, List<PageEntry>> sortedMap = new HashMap<>();
        for (PageEntry item : searchResult) {
            String key = item.getPdfName() + "|" + item.getPage();
            if (sortedMap.containsKey(key)) {
                sortedMap.get(key).add(item);
            } else {
                List<PageEntry> list = new ArrayList<>();
                list.add(item);
                sortedMap.put(key, list);
            }
        }
        List<PageEntry> finalResult = new ArrayList<>();
        for (Map.Entry<String, List<PageEntry>> entry : sortedMap.entrySet()) {
            List<PageEntry> sortedList = new ArrayList<>();
            sortedList.addAll(entry.getValue());
            if (sortedList.size() > 1) {
                int sumCount = 0;
                String pdfName = null;
                int pageNumber = 0;
                for (PageEntry element : sortedList) {
                    sumCount = sumCount + element.getCount();
                    pdfName = element.getPdfName();
                    pageNumber = element.getPage();
                }
                sortedList.clear();
                sortedList.add(new PageEntry(pdfName, pageNumber, sumCount));
                sortedMap.replace(entry.getKey(), sortedList);
            }
            finalResult.addAll(entry.getValue());
        }
        if (!finalResult.isEmpty()) {
            Collections.sort(finalResult, PageEntry::compareTo);
            return finalResult;
        }
        return Collections.emptyList();
    }

    public void loadStopTxt() {
        try (BufferedReader in = new BufferedReader(new FileReader("stop-ru.txt"))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] arr = line.split(" ");
                for (int i = 0; i < arr.length; i++) {
                    stopWords.add(arr[i].toLowerCase());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
