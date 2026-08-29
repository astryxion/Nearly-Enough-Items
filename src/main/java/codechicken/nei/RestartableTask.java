package codechicken.nei;

/**
 * Binary-compatible with ChickenBones/GTNH NEI. Addons such as NEI Recipe
 * Handlers call {@code ItemList.loadItems.restart()} and then join the worker
 * thread via the private {@code thread} field.
 */
public abstract class RestartableTask {

	public final String name;
	private Thread thread;
	private volatile boolean restart;
	private volatile boolean stopped;

	public RestartableTask(String name) {
		this.name = name;
	}

	private void start() {
		thread = new Thread(name) {
			@Override
			public void run() {
				do {
					while (stopped) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException ignored) {
						}
					}
					if (!restart) {
						execute();
					}
				} while (!finish());
			}
		};
		thread.start();
	}

	private synchronized boolean finish() {
		if (restart) {
			restart = false;
			return false;
		}
		clearTasks();
		return true;
	}

	public void clearTasks() {
		thread = null;
	}

	public synchronized void restart() {
		if (thread != null) {
			stopped = false;
			restart = true;
		} else {
			start();
		}
	}

	public synchronized void stop() {
		if (thread != null) {
			stopped = true;
			restart = true;
		}
	}

	public boolean interrupted() {
		return restart;
	}

	public abstract void execute();
}
