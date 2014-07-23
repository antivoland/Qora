package database.wallet;

import java.io.File;

import org.mapdb.Atomic.Var;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import qora.account.Account;
import settings.Settings;

public class WalletDatabase 
{
	private static final File WALLET_FILE = new File(Settings.getInstance().getWalletDir(), "wallet.dat");
	
	private static final String VERSION = "version";
	private static final String LAST_BLOCK = "lastBlock";
	
	private DB database;	
	private AccountsDatabase accountsDatabase;
	private TransactionsDatabase transactionsDatabase;
	private BlocksDatabase blocksDatabase;
	private NamesDatabase namesDatabase;
	private NameSalesDatabase nameSalesDatabase;
	private PollDatabase pollDatabase;
	
	public static boolean exists()
	{
		return WALLET_FILE.exists();
	}
	
	public WalletDatabase()
	{
		//OPEN WALLET
		WALLET_FILE.getParentFile().mkdirs();
		
		//DELETE TRANSACTIONS
		File transactionFile = new File(Settings.getInstance().getWalletDir(), "wallet.dat.t");
		transactionFile.delete();	
		
	    this.database = DBMaker.newFileDB(WALLET_FILE)
	    		.closeOnJvmShutdown()
	            .make();
	    
	    this.accountsDatabase = new AccountsDatabase(this, this.database);
	    this.transactionsDatabase = new TransactionsDatabase(this, this.database);
	    this.blocksDatabase = new BlocksDatabase(this, this.database);
	    this.namesDatabase = new NamesDatabase(this, this.database);
	    this.nameSalesDatabase = new NameSalesDatabase(this, this.database);
	    this.pollDatabase = new PollDatabase(this, this.database);
	}
	
	public void setVersion(int version)
	{
		this.database.getAtomicInteger(VERSION).set(version);
	}
	
	public int getVersion()
	{
		return this.database.getAtomicInteger(VERSION).intValue();
	}
	
	public void setLastBlockSignature(byte[] signature)
	{
		Var<byte[]> atomic = this.database.getAtomicVar(LAST_BLOCK);
		atomic.set(signature);
	}
	
	public byte[] getLastBlockSignature()
	{
		Var<byte[]> atomic = this.database.getAtomicVar(LAST_BLOCK);
		return atomic.get();
	}
	
	public AccountsDatabase getAccountsDatabase()
	{
		return this.accountsDatabase;
	}
	
	public TransactionsDatabase getTransactionsDatabase()
	{
		return this.transactionsDatabase;
	}
	
	public BlocksDatabase getBlocksDatabase()
	{
		return this.blocksDatabase;
	}
	
	public NamesDatabase getNamesDatabase()
	{
		return this.namesDatabase;
	}
	
	public NameSalesDatabase getNameSalesDatabase()
	{
		return this.nameSalesDatabase;
	}
	
	public PollDatabase getPollDatabase()
	{
		return this.pollDatabase;
	}
	
	public void delete(Account account)
	{
		this.accountsDatabase.delete(account);
		this.blocksDatabase.delete(account);
		this.transactionsDatabase.delete(account);
		this.namesDatabase.delete(account);
		this.nameSalesDatabase.delete(account);
		this.pollDatabase.delete(account);
	}
	
	public void commit()
	{
		this.database.commit();
	}
	
	public void close() 
	{
		if(this.database != null)
		{
			if(!this.database.isClosed())
			{
				this.database.commit();
				this.database.close();
			}
		}
	}
}

