package ri.wse.indexerBuild;

import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Contiene la lógica relacionada a la generación de los archivos resultados de correr el programa y generar los
 * cálculos necesarios para indexar la colección de documentos.
 */
public class FileManager {

    /**
     * Contiene el path donde se debe almacenar los resultados.
     */
    private static final String RESULTS_DIRECTORY = "./webSearchEngine-core/src/main/java/webSearchEngine/ri/resources/Results";

    private Map<String, Map<String, Double>> documents;

    private Map<String, Double> vocabulary;

    public FileManager(Map<String, Map<String, Double>> documents, Map<String, Double> vocabulary) {
        this.documents = documents;
        this.vocabulary = vocabulary;
    }

    /**
     * Escribe en los archivos completando la cantidad de espacios en blanco.
     *
     * @param writer
     * @param index
     * @param value
     */
    private void writeToFile(PrintWriter writer, int index, String value, boolean putNewLineIndexFile) {
        switch (index) {
            case 0: {
                this.writeValue(writer, value, 30);
                writer.print(" ");
                break;
            }
            case 1: {
                this.writeValue(writer, value, 12);
                if (putNewLineIndexFile) {
                    writer.println();
                } else {
                    writer.print(" ");
                }
                break;
            }
            case 2: {
                this.writeValue(writer, value, 20);
                writer.println();
                break;
            }
            default: {
                this.writeValue(writer, value, 31);
                writer.print(" ");
                break;
            }
        }
    }

    /**
     * Completa los espacios en blanco.
     *
     * @param writer
     * @param value
     * @param size
     */
    private void completeSpaces(PrintWriter writer, String value, int size) {
        if (value.length() < size) {
            for (int i = value.length() + 1; i <= size; i++) {
                value += " ";
            }
        }
        writer.print(value);
    }

    /**
     * Escribe valores en el archivo.
     *
     * @param writer
     * @param value
     * @param maxColumnSize
     */
    private void writeValue(PrintWriter writer, String value, int maxColumnSize) {
        if (value.length() > maxColumnSize) {
            value = value.substring(0, maxColumnSize);
            writer.print(value);
        } else {
            this.completeSpaces(writer, value, maxColumnSize);
        }
    }

    /**
     * Genera los archivos .tok que contienen:
     * -Palabra.
     * -Número de veces que aparece el término en el documento.
     * -Frecuencia normalizada.
     * y manda a escribir en ellos.
     */
    public void generateTokFiles() {
        double max;
        PrintWriter writer;

        for (Map.Entry<String, Map<String, Double>> entry : this.documents.entrySet()) {
            try {
                writer = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(RESULTS_DIRECTORY+ "/tok/" + entry.getKey().replace(".html", ".tok")),
                        StandardCharsets.UTF_8));                

                if (entry.getValue().entrySet().size() != 0) {
                    max = entry.getValue().entrySet().stream().max(Map.Entry.comparingByValue()).get().getValue();

                    for (Map.Entry<String, Double> word : entry.getValue().entrySet()) {
                        this.writeToFile(writer, 0, word.getKey(), false);
                        this.writeToFile(writer, 1, word.getValue().toString(), false);
                        this.writeToFile(writer, 2, Double.toString(word.getValue() / max), false);

                    }
                }

                writer.flush();
                writer.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Genera un archivo con el vocabulario, que contiene la cantidad de documentos en los
     * que aparece cada parabra y el idf.
     *
     * @param totalCollectionFiles
     */
    public void generateVocabularyFile(double totalCollectionFiles) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(RESULTS_DIRECTORY + "/Vocabulario.txt"), StandardCharsets.UTF_8));

            for (Map.Entry<String, Double> word : this.vocabulary.entrySet()) {
                this.writeToFile(writer, 0, word.getKey(), false);
                this.writeToFile(writer, 1, Double.toString(word.getValue()), false);
                this.writeToFile(writer, 2, Double.toString(Math.log10(totalCollectionFiles / word.getValue())), false);
            }

            writer.flush();
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Carga los términos del archivo Vocabulary en un mapa junto con su frecuencia inversa
     *
     * @return vocabulary Mapa que contiene el vocabulario
     */
    private Map<String, Double> loadVocabularyFile() {
        Map<String, Double> vocabulary = new TreeMap<>();

        try (Stream<String> stream = Files.lines(Paths.get(FileManager.RESULTS_DIRECTORY + "/Vocabulario.txt"))) {
            stream.forEach(line -> {
                vocabulary.put(line.substring(0, 30).trim(), Double.parseDouble(line.substring(43, line.length() - 1).trim()));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return vocabulary;
    }

    /**
     * Encuentra cuáles son cada uno de los archivos .tok que se deben recorrer, y almacena sus nombre en un array.
     *
     * @return tokFiles Arreglo con todos los nombres de los archivos de extensión .tok
     */
    private String[] findTokFiles() {
        File file = new File(FileManager.RESULTS_DIRECTORY + "/tok");
        FilenameFilter filter = (dir, fileName) -> fileName.endsWith(".tok");
        String[] tokFiles = file.list(filter);

        if (tokFiles == null) {
            System.err.println("No se encontraron archivos.");
        }

        return tokFiles;
    }

    /**
     * Genera el archivo Postings que contiene:
     * -Palabra.
     * -Alias del docuemnto en el que aparece.
     * -Peso (wij).
     * @param postingsValues Mapa con los términos y una lista formada por pares que corresponden al alias del
     *                       documento y el peso
     */
    private void generatePostingsFile(Map<String, ArrayList<Pair<String, Double>>> postingsValues) {
        try {
            final PrintWriter postingsWriter = new PrintWriter(RESULTS_DIRECTORY + "/Postings.txt");
            DecimalFormat df = new DecimalFormat("#.##################");

            postingsValues.forEach((term, pairsList) -> {
                pairsList.forEach(aliasWeightPair -> {
                    this.writeToFile(postingsWriter, 0, term, false);
                    this.writeToFile(postingsWriter, 3, aliasWeightPair.getKey().trim(), false);
                    this.writeToFile(postingsWriter, 2, df.format(aliasWeightPair.getValue()), false);
                });
            });

            postingsWriter.flush();
            postingsWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Genera el archivo Indice que contiene:
     * -Palabra.
     * -Número de línea que corresponde a la primera aparición en el archivo Postings.
     * -Cantidad de veces que aparece en este archivo.
     * @param postingsValues Mapa con los términos y la cantidad de veces que aparece ese término en el archivo postings
     */
    private void generateIndexFile(Map<String, Integer> postingsValues) {
        try {
            final PrintWriter indexWriter = new PrintWriter(RESULTS_DIRECTORY + "/Indice.txt");
            final int[] lineCount = {1};

            postingsValues.forEach((term, value) -> {
                this.writeToFile(indexWriter, 0, term, false);
                this.writeToFile(indexWriter, 1, Integer.toString(lineCount[0]), false);
                this.writeToFile(indexWriter, 1, Integer.toString(value), true);
                lineCount[0] += value;
            });

            indexWriter.flush();
            indexWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Genera los archivos .wtd que contienen:
     * -Palabra
     * -Peso (frecuencia normalizada * frecuncia inversa)
     * Y va llenando el mapa que se utiliza para generar después el archivo Postings y el Indice.
     * @throws FileNotFoundException
     */
    public void generateWtdPostingsIndexFiles() throws FileNotFoundException {
        Map<String, Double> vocabulary = this.loadVocabularyFile();
        Map<String, Integer> postingsValuesForIndex = new TreeMap<>();
        Map<String, ArrayList<Pair<String, Double>>> postingsValues = new TreeMap<>();
        String[] tokFiles = this.findTokFiles();

        Arrays.stream(tokFiles).forEach(tokFileName -> {
            try (Stream<String> stream = Files.lines(Paths.get(FileManager.RESULTS_DIRECTORY + "/tok/" + tokFileName))) {
                PrintWriter wtdWriter = new PrintWriter(RESULTS_DIRECTORY + "/wtd/" + tokFileName.replace(".tok", ".wtd"));
                stream.forEach(line -> {
                    String term = line.substring(0, 30).trim();
                    double normalizedFrequency = Double.parseDouble(line.substring(44, line.length() - 1).trim());
                    this.writeToFile(wtdWriter, 0, term, false);
                    this.writeToFile(wtdWriter, 2, Double.toString(vocabulary.get(term) * normalizedFrequency), false);

                    if (postingsValues.containsKey(term)) {
                        postingsValues.get(term).add(new Pair<>(tokFileName.substring(0, tokFileName.length() - 4), vocabulary.get(term) * normalizedFrequency));
                    } else {
                        postingsValues.put(term, new ArrayList<>(Collections.singletonList(new Pair<>(tokFileName.substring(0, tokFileName.length() - 4), vocabulary.get(term) * normalizedFrequency))));
                    }

                    if (postingsValuesForIndex.containsKey(term)) {
                        postingsValuesForIndex.put(term, postingsValuesForIndex.get(term) + 1);
                    } else {
                        postingsValuesForIndex.put(term, 1);
                    }
                });

                wtdWriter.flush();
                wtdWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.generatePostingsFile(postingsValues);
        this.generateIndexFile(postingsValuesForIndex);

    }

    public Map<String, Map<String, Double>> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<String, Map<String, Double>> documents) {
        this.documents = documents;
    }

    public Map<String, Double> getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Map<String, Double> vocabulary) {
        this.vocabulary = vocabulary;
    }

}
