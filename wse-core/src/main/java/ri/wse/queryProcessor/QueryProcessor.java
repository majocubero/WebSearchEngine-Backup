package ri.wse.queryProcessor;

import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryProcessor {

    /**
     * Dirección del archivo que contiene los "stopwords".
     */
    private static final String BASE_FILE_PATH = "..\\resources\\";

    /**
     * Contiene una lista de los "stopwords" que se recuperaron del archivo.
     */
    private List<String> stopWords;

    private String query;
    private Map<String, Query> queryTerms;
    private Map<String, Double> vocabulary;
    private Map<String, Map<String, Double>> postings;
    private Map<String, Double> similarityMap;
    private Map<String, String> urls;
    private List<Pair<String, String>> results;

    public QueryProcessor(String query) {
        this.query = query;
        this.queryTerms = new TreeMap<>();
        this.stopWords = new LinkedList<>();
        this.vocabulary = new TreeMap<>();
        this.postings = new TreeMap<>();
        this.similarityMap = new HashMap<>();
        this.urls = new HashMap<>();
        this.results = new LinkedList<>();
        this.loadStopWords();
    }

    /**
     * Parsea la consulta, hace los calculos necesarios y obtiene los documentos
     */
    public List<Pair<String, String>> manageQuery() {
        double maxFreq = 1;
        String[] text = this.query.split("-");
        for (String term : text) {
            term = term.trim();
            if (!term.equals("") && !term.equals(" ") && term.length() <= 30 && !this.stopWords.contains(term)
                    && !this.isSmallWord(term)) {
                if (!queryTerms.containsKey(term)) {
                    Query newQuery = new Query();
                    newQuery.setFreq(1);
                    queryTerms.put(term, newQuery);

                } else {
                    Query currentQuery = queryTerms.get(term);
                    currentQuery.setFreq(currentQuery.getFreq() + 1);
                    queryTerms.put(term, currentQuery);
                    if (currentQuery.getFreq() > maxFreq) {
                        maxFreq = currentQuery.getFreq();
                    }
                }
            }
        }
        this.generateQueryValues(maxFreq);
        this.loadPostingsFile();
        this.loadUrlsFile();
        this.getSimilarity();
        System.out.println(results);
        return this.results;
    }

    /**
     * Asigna los valores de f y w a la consulta.
     */
    private void generateQueryValues(double maxFreq) {
        this.loadVocabularyFile();
        for (Map.Entry<String, Query> word : this.queryTerms.entrySet()) {
            Query currentQuery = word.getValue();
            currentQuery.setF(currentQuery.getFreq() / maxFreq);
            currentQuery.setW((0.5 + (0.5 * currentQuery.getFreq())) * vocabulary.get(word.getKey()));
        }

    }

    /**
     * Verifica si una palabra tiene menos de 3 caracteres para no incluila como parte del vocabulario,
     * pero si tiene 1 o 2 caracteres y es un número, entoces sí se incluyen.
     *
     * @param word
     * @return
     */
    private boolean isSmallWord(String word) {
        if (word.length() > 2) {
            return false;
        }
        try {
            Integer.parseInt(word);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Carga los "stopwords" del archivo correspondiente y los mete en una lista enlazada.
     */
    private void loadStopWords() {
        try (Stream<String> stream = Files.lines(Paths.get(BASE_FILE_PATH + "stopwords.txt"))) {
            stream.forEach(this.stopWords::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga los términos del archivo Vocabulary en un mapa junto con su frecuencia inversa/
     **/
    private void loadVocabularyFile() {
        try (Stream<String> stream = Files.lines(Paths.get(BASE_FILE_PATH + "Results/Vocabulario.txt"))) {
            stream.forEach(line -> {
                this.vocabulary.put(line.substring(0, 30).trim(), Double.parseDouble(line.substring(43, line.length() - 1).trim()));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga los documentos y terminos del archivo Postings en un mapa junto con su peso
     **/
    private void loadPostingsFile() {
        try (Stream<String> stream = Files.lines(Paths.get(BASE_FILE_PATH + "Results/Postings.txt"))) {
            stream.forEach(line -> {
                String word = line.substring(0, 30).trim();
                String documentName = line.substring(31, 62).trim();
                double w = Double.parseDouble(line.substring(63, line.length() - 1).trim());
                if (postings.containsKey(documentName)) {
                    Map<String, Double> currentMap = postings.get(documentName);
                    currentMap.put(word, w);
                } else {
                    Map<String, Double> values = new TreeMap<>();
                    values.put(word, w);
                    postings.put(documentName, values);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calcula la similaridad entre la consulta y los documentos y los agrega a un mapa
     */
    private void getSimilarity() {
        Map<String, Double> unsortedResults = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> currentDocument : this.postings.entrySet()) {
            Map<String, Double> values = currentDocument.getValue();
            double firstSum = 0.0;
            double secondSum = 0.0;
            double thirdSum = 0.0;
            for (Map.Entry<String, Double> currentValue : values.entrySet()) {
                secondSum += currentValue.getValue() * currentValue.getValue();
                if (queryTerms.containsKey(currentValue.getKey())) {
                    Query currentQuery = queryTerms.get(currentValue.getKey());
                    firstSum += currentValue.getValue() * currentQuery.getW();
                } else {
                    firstSum += 0;
                }
            }
            for (Map.Entry<String, Query> currentQuery : queryTerms.entrySet()) {
                thirdSum += currentQuery.getValue().getW() * currentQuery.getValue().getW();
            }
            double similarity = firstSum / (Math.sqrt(secondSum) * Math.sqrt(thirdSum));
            if (similarity > 0) {
                unsortedResults.put(currentDocument.getKey().trim(), similarity);
            }
            this.similarityMap = unsortedResults.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
        }

        for (Map.Entry<String, Double> currentDocument : similarityMap.entrySet()) {
            this.results.add(new Pair<>(currentDocument.getKey(), urls.get(currentDocument.getKey())));
        }
        System.out.println(results);
    }

    /**
     * Carga los urls de los archivos en un mapa
     */
    private void loadUrlsFile() {
        try (Stream<String> stream = Files.lines(Paths.get(BASE_FILE_PATH + "URLS.txt"))) {
            stream.forEach(line -> {
                String[] currentLine = line.split(" ");
                currentLine[0] = currentLine[0].replace(".html", "").trim();
                urls.put(currentLine[0], currentLine[1]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        QueryProcessor q = new QueryProcessor("roger federer tennis");
        q.manageQuery();
    }

}
