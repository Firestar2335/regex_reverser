public class MultiParserThread implements Runnable {
	private final MultiContext con;

	public MultiParserThread(MultiContext context) {
		con = context;
	}
	
	public void run() {
		while (!con.isEmpty()) {
			try {
				MultiParser.Group task = con.take();
				MultiParser.MultiResult res= MultiParser.multiParse(con.str, task.start, task.stop);
				for (MultiParser.Group g : res.groups) {
					g.indices.addAll(0,task.indices);
				}
				con.toMaster.put(new MultiParser.ReceiveResult(res.tokens, task));
				con.putAll(res.groups);
				//System.out.println(res);
				con.taskDone();
			}
			catch (InterruptedException e) {
				break;//Exit
			}
		}
	}
}
