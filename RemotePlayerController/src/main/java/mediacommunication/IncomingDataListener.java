package mediacommunication;

/**
 * used by server to notify every listener of new incoming data, that need to synced
 * @author mat
 */
public interface IncomingDataListener {

	public void SyncDataIncoming(DataForSync incoming);

}
