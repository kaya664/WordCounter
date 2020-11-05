import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CounterWorker {

	private ExecutorService pool;
	
	public CounterWorker(int threadCount) {
		pool = Executors.newFixedThreadPool(threadCount);
	}
	
	public Future<Boolean> findWordsInSentence(String sentence) {
		return pool.submit(() -> {
			WordCounter.addWordToLibrary(sentence.trim().split(",| |\r\n"));
			return Boolean.TRUE;
		});
	}
	
	public void shutdown() {
		pool.shutdown();
	}
}
