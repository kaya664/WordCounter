import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class WordCounter {
	//total sentence count
	private static Integer sentenceCount = 0;
	//total word count
	private static Integer totalWordCount = 0;
	//default thread count
	private static Integer threadCount = 5;
	//map for word + totalWordCount entry
	private static Map<String, Integer> wordCountMap = new HashMap<String, Integer>();

	public static void main(String[] args) throws IOException {
		//validates given args
		Boolean argumentsValid = areArgumentsValid(args);
		if (argumentsValid) {
			//get the filepath to read
			String filePath = readFilePath(args);
			//get whole content of the file as string
			String fileContent = readFileAsString(filePath);
			//splits content into sentences and starts worker threads 
			findAllWordsWithCountInformation(fileContent);
			//prints the word counter result
			printWordCounterResult();
		}
	}
	
	//creates requested amount of threads and starts workers to compute sentences, then waits for threads
	@SuppressWarnings("rawtypes")
	public static void findAllWordsWithCountInformation(String content) {
		CounterWorker counterWorker = new CounterWorker(threadCount);
		List<Future> futureList = new ArrayList<Future>();
		for (String sentence : getSentencesAsArray(content)) {
			Future<Boolean> future = counterWorker.findWordsInSentence(sentence);
			futureList.add(future);
		}

		while (!isWorkersDone(futureList)) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		counterWorker.shutdown();
	}
	
	//sync method for workers to change word counts
	public static synchronized void addWordToLibrary(String[] wordArray) {
		sentenceCount++;
		for (String word : wordArray) {
			if (word != null && !"".equals(word.trim())) {
				if (wordCountMap.containsKey(word)) {
					wordCountMap.replace(word, wordCountMap.get(word) + 1);
				} else {
					wordCountMap.put(word, 1);
				}
				totalWordCount++;
			}
		}
	}
	
	//prints the requested result
	public static void printWordCounterResult() {
		System.out.println("Sentence Count : " + sentenceCount);
		System.out.println("Avg. Word Count : " + totalWordCount / sentenceCount);

		wordCountMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEach(entry -> {
					System.out.println(entry.getKey() + " " + entry.getValue());
				});
	}
	
	//checks if created workers are done
	@SuppressWarnings("rawtypes")
	public static Boolean isWorkersDone(List<Future> futureList) {
		Boolean result = Boolean.TRUE;
		for (Future future : futureList) {
			result = result && future.isDone();
		}
		return result;
	}

	public static Boolean areArgumentsValid(String args[]) {
		Boolean result = Boolean.TRUE;
		if (args == null || args.length < 1) {
			System.out.println("At least filePath(arg[0]) argument is required.");
			result = Boolean.FALSE;
		} else if (!isFilePathValid(args[0])) {
			System.out.println("FilePath is not valid. " + args[0]);
			result = Boolean.FALSE;
		} else if (args.length > 1 && args[1] != null && !isThreadCountValid(args[1])) {
			System.out.println("Thread Count is not required variable and must be non-negative Integer. (Default 5)");
			result = Boolean.FALSE;
		}
		return result;
	}

	public static Boolean isFilePathValid(String filePath) {
		return new File(filePath).exists();
	}

	public static Boolean isThreadCountValid(String threadCountText) {
		Boolean result = Boolean.TRUE;
		try {
			threadCount = Integer.parseInt(threadCountText);
		} catch (Exception e) {
			result = Boolean.FALSE;
		}
		return result;
	}

	public static String readFilePath(String args[]) {
		String filePath = "";
		if (args != null && args[0] != null) {
			filePath = args[0];
		}
		return filePath;
	}

	public static String readFileAsString(String filePath) throws IOException {
		StringBuilder sb = new StringBuilder();
		if (filePath != null && filePath.trim().length() > 0) {
			List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
			for (String line : lines) {
				sb.append(line + ",");
			}
		}
		return sb.toString().substring(0, sb.toString().length() - 1);
	}

	public static String[] getSentencesAsArray(String content) {
		String[] sentenceArray = null;
		if (content != null) {
			sentenceArray = content.split("[.?!]");
		}
		return sentenceArray;
	}

}
