
package LMS;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Library {

	private String name;                                // name of library
	private Librarian librarian;                        // object of Librarian (only one)                       
	private ArrayList <Person> persons;                 // all clerks and borrowers  
	private ArrayList <Book> booksInLibrary;            // all books in library are here!

	private ArrayList <Loan> loans;                     // history of all books which have been issued

	public int book_return_deadline;                   //return deadline after which fine will be generated each day
	public double per_day_fine;

	public int hold_request_expiry;                    //number of days after which a hold request will expire


	/*----Following Singleton Design Pattern (Lazy Instantiation)------------*/
	private static Library obj;

	public static Library getInstance()
	{
		if(obj==null)
		{
			obj = new Library();
		}

		return obj;
	}
	/*---------------------------------------------------------------------*/

	private Library()   // default cons.
	{
		name = null;
		librarian = null;
		persons = new ArrayList();

		booksInLibrary = new ArrayList();
		loans = new ArrayList();
	}


	/*------------Setter FUNCs.------------*/

	public void setReturnDeadline(int deadline)
	{
		book_return_deadline = deadline;
	}

	public void setFine(double perDayFine)
	{
		per_day_fine = perDayFine;
	}

	public void setRequestExpiry(int hrExpiry)
	{
		hold_request_expiry = hrExpiry;
	}
	/*--------------------------------------*/    



	// Setter Func.
	public void setName(String n)   
	{
		name = n;
	}

	/*-----------Getter FUNCs.------------*/

	public int getHoldRequestExpiry()
	{
		return hold_request_expiry;
	}

	public ArrayList<Person> getPersons()
	{
		return persons;
	}

	public Librarian getLibrarian()
	{
		return librarian;
	}

	public String getLibraryName()
	{
		return name;
	}

	public ArrayList<Book> getBooks()
	{
		return booksInLibrary;
	}

	/*---------------------------------------*/

	/*-----Adding all People in Library----*/
	public boolean addLibrarian(Librarian lib)
	{
		//One Library can have only one Librarian
		if (librarian == null)
		{
			librarian = lib;
			persons.add(librarian);
			return true;
		}
		else
			System.out.println("\nSorry, the library already has one librarian. New Librarian can't be created.");
		return false;
	}

	public void addClerk(Clerk c) 
	{
		persons.add(c);
	}

	public void addBorrower(Borrower b)
	{
		persons.add(b);
	}


	public void addLoan(Loan l)
	{
		loans.add(l);
	}

	/*----------------------------------------------*/

	/*-----------Finding People in Library--------------*/
	public Borrower findBorrower()
	{
		System.out.println("\nEnter Borrower's ID: ");

		int id = 0;

		Scanner scanner = new Scanner(System.in);

		try{
			id = scanner.nextInt();
		}
		catch (java.util.InputMismatchException e)
		{
			System.out.println("\nInvalid Input");
		}

		for (int i = 0; i < persons.size(); i++)
		{
			if (persons.get(i).getID() == id && persons.get(i).getClass().getSimpleName().equals("Borrower"))
				return (Borrower)(persons.get(i));
		}

		System.out.println("\nSorry this ID didn't match any Borrower's ID.");
		return null;
	}

	public Clerk findClerk()
	{
		System.out.println("\nEnter Clerk's ID: ");

		int id = 0;

		Scanner scanner = new Scanner(System.in);

		try{
			id = scanner.nextInt();
		}
		catch (java.util.InputMismatchException e)
		{
			System.out.println("\nInvalid Input");
		}

		for (int i = 0; i < persons.size(); i++)
		{
			if (persons.get(i).getID() == id && persons.get(i).getClass().getSimpleName().equals("Clerk"))
				return (Clerk)(persons.get(i));
		}

		System.out.println("\nSorry this ID didn't match any Clerk's ID.");
		return null;
	}

	/*------- FUNCS. on Books In Library--------------*/
	public void addBookinLibrary(Book b)
	{
		booksInLibrary.add(b);
	}

	public void removeBookfromLibrary(Book b)  
	{
		boolean delete = true;

		//Checking if this book is currently borrowed by some borrower
		for (int i = 0; i < persons.size() && delete; i++)
		{
			if (persons.get(i).getClass().getSimpleName().equals("Borrower"))
			{
				ArrayList<Loan> borBooks = ((Borrower)(persons.get(i))).getBorrowedBooks();

				for (int j = 0; j < borBooks.size() && delete; j++)
				{
					if (borBooks.get(j).getBook() == b)
					{
						delete = false;
						System.out.println("This particular book is currently borrowed by some borrower.");
					}
				}              
			}
		}

		if (delete)
		{
			System.out.println("\nCurrently this book is not borrowed by anyone.");
			ArrayList<HoldRequest> hRequests = b.getHoldRequests();

			if(!hRequests.isEmpty())
			{
				System.out.println("\nThis book might be on hold requests by some borrowers. Deleting this book will delete the relevant hold requests too.");
				System.out.println("Do you still want to delete the book? (y/n)");

				Scanner sc = new Scanner(System.in);

				while (true)
				{
					String choice = sc.next();

					if(choice.equals("y") || choice.equals("n"))
					{
						if(choice.equals("n"))
						{
							System.out.println("\nDelete Unsuccessful.");
							return;
						}                            
						else
						{
							//Empty the books hold request array
							//Delete the hold request from the borrowers too
							for (int i = 0; i < hRequests.size() && delete; i++)
							{
								HoldRequest hr = hRequests.get(i);
								hr.getBorrower().removeHoldRequest(hr);
								b.removeHoldRequest();                                                                
							}
						}
					}
					else
						System.out.println("Invalid Input. Enter (y/n): ");
				}

			}
			else
				System.out.println("This book has no hold requests.");

			booksInLibrary.remove(b);
			System.out.println("The book is successfully removed.");
		}
		else
			System.out.println("\nDelete Unsuccessful.");
	}



	// Searching Books on basis of title, Subject or Author 
	public ArrayList<Book> searchForBooks() throws IOException
	{
		String choice;
		String title = "", subject = "", author = "";

		Scanner sc = new Scanner(System.in);  
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			System.out.println("\nEnter either '1' or '2' or '3' for search by Title, Subject or Author of Book respectively: ");  
			choice = sc.next();

			if (choice.equals("1") || choice.equals("2") || choice.equals("3"))
				break;
			else
				System.out.println("\nWrong Input!");
		}

		if (choice.equals("1"))
		{
			System.out.println("\nEnter the Title of the Book: ");              
			title = reader.readLine();  
		}

		else if (choice.equals("2"))
		{
			System.out.println("\nEnter the Subject of the Book: ");              
			subject = reader.readLine();  
		}

		else
		{
			System.out.println("\nEnter the Author of the Book: ");              
			author = reader.readLine();              
		}

		ArrayList<Book> matchedBooks = new ArrayList();

		//Retrieving all the books which matched the user's search query
		for(int i = 0; i < booksInLibrary.size(); i++)
		{
			Book b = booksInLibrary.get(i);

			if (choice.equals("1"))
			{ 
				if (b.getTitle().equals(title))
					matchedBooks.add(b);
			}
			else if (choice.equals("2"))
			{ 
				if (b.getSubject().equals(subject))
					matchedBooks.add(b);
			}
			else
			{
				if (b.getAuthor().equals(author))
					matchedBooks.add(b);                
			}
		}

		//Printing all the matched Books
		if (!matchedBooks.isEmpty())
		{
			System.out.println("\nThese books are found: \n");

			System.out.println("------------------------------------------------------------------------------");            
			System.out.println("No.\t\tTitle\t\t\tAuthor\t\t\tSubject");
			System.out.println("------------------------------------------------------------------------------");

			for (int i = 0; i < matchedBooks.size(); i++)
			{                      
				System.out.print(i + "-" + "\t\t");
				matchedBooks.get(i).printInfo();
				System.out.print("\n");
			}

			return matchedBooks;
		}
		else
		{
			System.out.println("\nSorry. No Books were found related to your query.");
			return null;
		}
	}



	// View Info of all Books in Library
	public void viewAllBooks()
	{
		if (!booksInLibrary.isEmpty())
		{ 
			System.out.println("\nBooks are: ");

			System.out.println("------------------------------------------------------------------------------");            
			System.out.println("No.\t\tTitle\t\t\tAuthor\t\t\tSubject");
			System.out.println("------------------------------------------------------------------------------");

			for (int i = 0; i < booksInLibrary.size(); i++)
			{                      
				System.out.print(i + "-" + "\t\t");
				booksInLibrary.get(i).printInfo();
				System.out.print("\n");
			}
		}
		else
			System.out.println("\nCurrently, Library has no books.");                
	}


	//Computes total fine for all loans of a borrower
	public double computeFine2(Borrower borrower)
	{
		System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------");            
		System.out.println("No.\t\tBook's Title\t\tBorrower's Name\t\t\tIssued Date\t\t\tReturned Date\t\t\t\tFine(Rs)");
		System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------");        

		double totalFine = 0;        
		double per_loan_fine = 0;

		for (int i = 0; i < loans.size(); i++)
		{
			Loan l = loans.get(i);

			if ((l.getBorrower() == borrower))
			{
				per_loan_fine = l.computeFine1();
				System.out.print(i + "-" + "\t\t" + loans.get(i).getBook().getTitle() + "\t\t\t" + loans.get(i).getBorrower().getName() + "\t\t" + loans.get(i).getIssuedDate() +  "\t\t\t" + loans.get(i).getReturnDate() + "\t\t\t\t" + per_loan_fine  + "\n");                

				totalFine += per_loan_fine;
			}            
		}

		return totalFine;
	}


	public void createPerson(char x)
	{
		Scanner sc = new Scanner(System.in);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("\nEnter Name: ");
		String n = "";
		try {
			n = reader.readLine();
		} catch (IOException ex) {
			Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.out.println("Enter Address: ");
		String address = "";
		try {
			address = reader.readLine();
		} catch (IOException ex) {
			Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
		}

		int phone = 0;

		try{
			System.out.println("Enter Phone Number: ");
			phone = sc.nextInt();
		}
		catch (java.util.InputMismatchException e)
		{
			System.out.println("\nInvalid Input.");
		}

		//If clerk is to be created
		if (x == 'c')
		{
			double salary = 0;

			try{
				System.out.println("Enter Salary: ");
				salary = sc.nextDouble();
			}
			catch (java.util.InputMismatchException e)
			{
				System.out.println("\nInvalid Input.");
			}

			Clerk c = new Clerk(-1,n,address,phone,salary,-1);            
			addClerk(c);

			System.out.println("\nClerk with name " + n + " created successfully.");
			System.out.println("\nYour ID is : " + c.getID());
			System.out.println("Your Password is : " + c.getPassword());
		}

		//If librarian is to be created
		else if (x == 'l')
		{
			double salary = 0;            
			try{
				System.out.println("Enter Salary: ");
				salary = sc.nextDouble();
			}
			catch (java.util.InputMismatchException e)
			{
				System.out.println("\nInvalid Input.");
			}

			Librarian l = new Librarian(-1,n,address,phone,salary,-1); 
			if(addLibrarian(l))
			{
				System.out.println("\nLibrarian with name " + n + " created successfully.");
				System.out.println("\nYour ID is : " + l.getID());
				System.out.println("Your Password is : " + l.getPassword());
			}
		}

		//If borrower is to be created
		else
		{
			Borrower b = new Borrower(-1,n,address,phone);
			addBorrower(b);            
			System.out.println("\nBorrower with name " + n + " created successfully.");

			System.out.println("\nYour ID is : " + b.getID());
			System.out.println("Your Password is : " + b.getPassword());            
		}        
	}



	public void createBook(String title, String subject, String author)
	{
		Book b = new Book(-1,title,subject,author,false);

		addBookinLibrary(b);

		System.out.println("\nBook with Title " + b.getTitle() + " is successfully created.");
	}



	// Called when want an access to Portal
	public Person login()
	{
		Scanner input = new Scanner(System.in);

		int id = 0;
		String password = "";

		System.out.println("\nEnter ID: ");

		try{
			id = input.nextInt();
		}
		catch (java.util.InputMismatchException e)
		{
			System.out.println("\nInvalid Input");
		}

		System.out.println("Enter Password: ");
		password = input.next();

		for (int i = 0; i < persons.size(); i++)
		{
			System.out.println("Password is: " + persons.get(i).getPassword());
			if (persons.get(i).getID() == id && persons.get(i).getPassword().equals(password))
			{
				System.out.println("\nLogin Successful");
				return persons.get(i);
			}
		}

		if(librarian!=null)
		{
			if (librarian.getID() == id && librarian.getPassword().equals(password))
			{
				System.out.println("\nLogin Successful");
				return librarian;
			}
		}

		System.out.println("\nSorry! Wrong ID or Password");        
		return null;
	}


	// History when a Book was Issued and was Returned!
	public void viewHistory()
	{
		if (!loans.isEmpty())
		{ 
			System.out.println("\nIssued Books are: ");

			System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");            
			System.out.println("No.\tBook's Title\tBorrower's Name\t  Issuer's Name\t\tIssued Date\t\t\tReceiver's Name\t\tReturned Date\t\tFine Paid");
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");

			for (int i = 0; i < loans.size(); i++)
			{    
				if(loans.get(i).getIssuer()!=null)
					System.out.print(i + "-" + "\t" + loans.get(i).getBook().getTitle() + "\t\t\t" + loans.get(i).getBorrower().getName() + "\t\t" + loans.get(i).getIssuer().getName() + "\t    " + loans.get(i).getIssuedDate());

				if (loans.get(i).getReceiver() != null)
				{
					System.out.print("\t" + loans.get(i).getReceiver().getName() + "\t\t" + loans.get(i).getReturnDate() +"\t   " + loans.get(i).getFineStatus() + "\n");
				}
				else
					System.out.print("\t\t" + "--" + "\t\t\t" + "--" + "\t\t" + "--" + "\n");
			}
		}
		else
			System.out.println("\nNo issued books.");                        
	}

	//---------------------------------------------------------------------------------------//
	/*--------------------------------IN - COLLABORATION WITH DATA BASE------------------------------------------*/


	// Loading all info in code via Database.
	public void populateLibrary() throws IOException, java.text.ParseException, ParseException
	{       
		Library lib = this;

		/* --- Populating Book ----*/

		File booksFile = new File("books.json");

		if(booksFile.length() == 0)
		{
			System.out.println("\nNo Books Found in Library"); 
		}
		else
		{
			int maxID = 0;
			JSONObject jObj = (JSONObject) new JSONParser().parse(new FileReader(booksFile));
			Set<Integer> ids = jObj.keySet();
			System.out.println(ids);
			Iterator<Integer> it = ids.iterator();

			while(it.hasNext()) 
			{
				Object i = it.next();
				JSONObject jo = (JSONObject) jObj.get(i);
				String author = (String) jo.get("AUTHOR");
				String title = (String) jo.get("TITLE");
				String subject = (String) jo.get("SUBJECT");
				int id = Integer.parseInt(i.toString());
				boolean issued = Boolean.getBoolean(jo.get("IS_ISSUED").toString());
				Book b = new Book(id, title, subject, author, issued);
				addBookinLibrary(b);

				if (maxID < id)
					maxID = id;
			}

			// setting Book Count
			Book.setIDCount(maxID);
		}

		/* ----Populating Clerks----*/

		File personFile = new File("people.json");
		File staffFile = new File("staff.json");            
		File clerksFile = new File("clerks.json");

		String[] reqColumns = new String[] {"PNAME", "ADDRESS", "PASSWORD", "PHONE_NO", "SALARY", "DESK_NO"};
		File[] files = new File[] {clerksFile, personFile, staffFile};
		JSONObject jObj = extractInfo(3, files, reqColumns);
		Set<Integer> ids = jObj.keySet();
		Iterator<Integer> it = ids.iterator();

		while(it.hasNext()) 
		{
			Object ID = it.next();
			LinkedHashMap jo = (LinkedHashMap) jObj.get(ID);
			String cname = (String) jo.get("PNAME");
			String adrs = (String) jo.get("ADDRESS"); 
			int phn = Integer.parseInt(jo.get("PHONE_NO").toString());
			double sal = Double.parseDouble(jo.get("SALARY").toString());
			int desk = Integer.parseInt(jo.get("DESK_NO").toString());
			int id = Integer.parseInt(ID.toString());
			Clerk c = new Clerk(id,cname,adrs,phn,sal,desk);

			addClerk(c);                                
		}

		/*-----Populating Librarian---*/

		File librarianFile = new File("librarian.json");
		files = new File[] {librarianFile, personFile, staffFile};
		reqColumns = new String[] {"PNAME", "ADDRESS", "PASSWORD", "PHONE_NO", "SALARY", "DESK_NO"};

		if (librarianFile.length() == 0)
		{
			System.out.println("No librarian found!");
		}
		else
		{
			jObj = extractInfo(3, files, reqColumns);
			ids = jObj.keySet();
			it = ids.iterator();

			while(it.hasNext()) 
			{
				Object ID = it.next();
				LinkedHashMap jo = (LinkedHashMap) jObj.get(ID);
				String lname = (String) jo.get("PNAME");
				String adrs = (String) jo.get("ADDRESS"); 
				int phn = Integer.parseInt(jo.get("PHONE_NO").toString());
				double sal = Double.parseDouble(jo.get("SALARY").toString());
				int office = Integer.parseInt(jo.get("DESK_NO").toString());
				int id = Integer.parseInt(ID.toString());
				Librarian l= new Librarian(id,lname,adrs,phn,sal,office);

				addLibrarian(l);                              
			}
		}

		/*---Populating Borrowers (partially)!!!!!!--------*/

		File borrowerFile = new File("borrower.json");
		files = new File[] {borrowerFile, personFile};
		reqColumns = new String[] {"PNAME", "ADDRESS", "PASSWORD", "PHONE_NO"};



		if (borrowerFile.length() == 0)
		{
			System.out.println("No borrower found!");
		}
		else
		{
			jObj = extractInfo(2, files, reqColumns);
			ids = jObj.keySet();
			it = ids.iterator();

			while(it.hasNext()) 
			{
				Object ID = it.next();
				LinkedHashMap jo = (LinkedHashMap) jObj.get(ID);
				String lname = (String) jo.get("PNAME");
				String adrs = (String) jo.get("ADDRESS"); 
				int phn = Integer.parseInt(jo.get("PHONE_NO").toString());
				int id = Integer.parseInt(ID.toString());
				Borrower b= new Borrower(id, name, adrs, phn);
				addBorrower(b);                             
			}

		}

		/*----Populating Loan----*/

		File loanFile = new File("loan.json");

		if (loanFile.length() == 0) 
		{
			System.out.println("No books issued yet!");
		}
		else
		{
			jObj = (JSONObject) new JSONParser().parse(new FileReader(loanFile));
			ids = jObj.keySet();
			it = ids.iterator();

			while(it.hasNext())
			{

				Object ID = it.next();
				JSONObject jo = (JSONObject) jObj.get(ID);
//				System.out.println("Printing... " + jo.get("BORROWER").toString());
				int borid = Integer.parseInt(jo.get("BORROWER").toString());
				int bokid = Integer.parseInt(jo.get("BOOK").toString());
				int iid = Integer.parseInt(jo.get("ISSUER").toString());
				Integer rid;
				if (jo.get("RECEIVER") == null) {rid = null;}
				else rid = Integer.parseInt(jo.get("RECEIVER").toString());
				int rd=0;
				Date rdate;
				String issueDate = jo.get("ISS_DATE").toString();
				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
				Date idate = df.parse(issueDate);
				//				Date idate=new Date (rs.getTimestamp("ISS_DATE").getTime());

				// s[0] = Issuer, s[1] = receiver
				Staff s[]=new Staff[2];
				boolean set=true;
				boolean fineStatus = Boolean.getBoolean(jo.get("FINE_PAID").toString());
				Borrower bb = null;

				// Extracts the data of issuer of the book
				// If the book was issued by the librarian
				if(iid == getLibrarian().getID())
				{
					s[0]=getLibrarian();
				}

				else
				{                                
					for(int k = 0; k < getPersons().size() && set; k++)
					{
						// If the book is issued by the clerk
						if(getPersons().get(k).getID() == iid && getPersons().get(k).getClass().getSimpleName().equals("Clerk"))
						{
							set=false;
							s[0]=(Clerk)(getPersons().get(k));
						}
					}
				}

				String returnDate;
				if (jo.get("RET_DATE") == null) { returnDate = null; }
				else { returnDate = jo.get("RET_DATE").toString(); }
				// Checks if book is returned or not
				if(rid != null)    // if there is a receiver 
				{
					rdate = df.parse(returnDate);
					//					rdate = new Date (rs.getTimestamp("RET_DATE").getTime()); 
					rd = (int) rid;

					if(rd == getLibrarian().getID())
						s[1] = getLibrarian();

					else
					{    //System.out.println("ff");
						for(int k = 0; k < getPersons().size() && set; k++)
						{
							if(getPersons().get(k).getID() == rd && getPersons().get(k).getClass().getSimpleName().equals("Clerk"))
							{
								set = false;
								s[1] = (Clerk) (getPersons().get(k));
							}
						}
					}

				}
				else
				{
					s[1] = null;  // no receiver
					rdate = null;
				}

				// Creates the borrower object
				for(int i = 0; i < getPersons().size() && set; i++)
				{
					if(getPersons().get(i).getID() == borid)
					{
						set = false;
						bb = (Borrower)(getPersons().get(i));
					}
				}

				set = true;

				ArrayList<Book> books = getBooks();

				for(int k = 0; k < books.size() && set; k++)
				{
					if(books.get(k).getID() == bokid)
					{
						set = false;   
						Loan l = new Loan(bb, books.get(k), s[0], s[1], idate, rdate, fineStatus);
						loans.add(l);
					}
				}
			}
		}

		/*----Populationg Hold Books----*/

		File holdRequestFile = new File("onHoldBooks.json");


		if (holdRequestFile.length() == 0) 
		{
			System.out.println("No books on hold yet!");
		}
		else
		{
			jObj = (JSONObject) new JSONParser().parse(new FileReader(holdRequestFile));
			ids = jObj.keySet();
			System.out.println(ids);
			it = ids.iterator();

			while(it.hasNext())
			{
				Object ID = it.next();
				JSONObject jo = (JSONObject) jObj.get(ID);
				int borid = Integer.parseInt(jo.get("BORROWER").toString());
				int bokid = Integer.parseInt(jo.get("BOOK").toString());
				String reqDate = jo.get("REQ_DATE").toString();
				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
				Date off = df.parse(reqDate);

				boolean set=true;
				Borrower bb =null;

				ArrayList<Person> persons = lib.getPersons();

				for(int i=0;i<persons.size() && set;i++)
				{
					if(persons.get(i).getID()==borid)
					{
						set=false;
						bb=(Borrower)(persons.get(i));
					}
				}

				set=true;

				ArrayList<Book> books = lib.getBooks();

				for(int i=0;i<books.size() && set;i++)
				{
					if(books.get(i).getID()==bokid)
					{
						set=false;   
						HoldRequest hbook= new HoldRequest(bb,books.get(i),off);
						books.get(i).addHoldRequest(hbook);
						bb.addHoldRequest(hbook);
					}
				}
			}
		}

		/* --- Populating Borrower's Remaining Info----*/

		for (int i = 0; i < loans.size(); i++) {
			int b_id = loans.get(i).getBorrower().getID();			//Borrower's ID
			if (loans.get(i).getReceiver() == null) {
				for (int j = 0; j < persons.size(); j++) {
					if (persons.get(j).getID() == b_id) {
						loans.get(i).getBorrower().addBorrowedBook(loans.get(i));
					}
				}
			}
		}


		ArrayList<Person> persons = lib.getPersons();

		/* Setting Person ID Count */
		int max=0;

		for(int i=0;i<persons.size();i++)
		{
			if (max < persons.get(i).getID())
				max=persons.get(i).getID();
		}

		Person.setIDCount(max);  
	}


	// Filling Changes back to Database
	@SuppressWarnings("unchecked")
	public void fillItBack() throws FileNotFoundException
	{
		Library lib = this;

		/* Filling Person's Table*/
		JSONObject finalObj = new JSONObject();
		for(int i=0;i<lib.getPersons().size();i++)
		{
			Map<String, String> m = new LinkedHashMap<String, String>();
			m.put("PNAME", lib.getPersons().get(i).getName());
			m.put("PASSWORD", lib.getPersons().get(i).getPassword());
			m.put("ADDRESS", lib.getPersons().get(i).getAddress());
			m.put("PHONE_NO", Integer.toString(lib.getPersons().get(i).getPhoneNumber()));

			finalObj.put(lib.getPersons().get(i).getID(), m);

		}
		PrintWriter pw = new PrintWriter("people.json");
		pw.write(finalObj.toJSONString());
		pw.close();
		System.out.println("People db populated!");

		/* Filling Clerk's Table and Staff Table*/
		JSONObject staffJson = new JSONObject();
		JSONObject clerkJson = new JSONObject();
		for(int i=0;i<lib.getPersons().size();i++)
		{
			if (lib.getPersons().get(i).getClass().getSimpleName().equals("Clerk"))
			{
				Map<String, String> m = new LinkedHashMap<String, String>();
				staffJson.put(lib.getPersons().get(i).getID(), m);
				m.put("TYPE", "Clerk");
				m.put("SALARY", Double.toString(((Clerk)(lib.getPersons().get(i))).getSalary()));

				clerkJson.put(lib.getPersons().get(i).getID(), ((Clerk)(lib.getPersons().get(i))).deskNo);	
			}

		}
		pw = new PrintWriter("clerks.json");
		pw.write(clerkJson.toJSONString());
		pw.close();

		JSONObject librarianJson = new JSONObject();
		if(lib.getLibrarian() != null)    // if  librarian is there
		{	
			Map<String, String> m = new LinkedHashMap<String, String>();
			staffJson.put(lib.getLibrarian().getID(), m);
			m.put("TYPE", "Librarian");
			m.put("SALARY", Double.toString(lib.getLibrarian().getSalary()));

			librarianJson.put(lib.getLibrarian().getID(), lib.getLibrarian().officeNo);  
		}
		pw = new PrintWriter("librarian.json");
		pw.write(librarianJson.toJSONString());
		pw.close();
		pw = new PrintWriter("staff.json");
		pw.write(staffJson.toJSONString());
		pw.close();

		/* Filling Borrower's Table*/
		JSONObject borrowerJson = new JSONObject();
		for(int i=0;i<lib.getPersons().size();i++)
		{
			if (lib.getPersons().get(i).getClass().getSimpleName().equals("Borrower"))
			{
				borrowerJson.put(lib.getPersons().get(i).getID(), "BORROWER");   
			}
		}
		pw = new PrintWriter("borrower.json");
		pw.write(borrowerJson.toJSONString());
		pw.close();

		ArrayList<Book> books = lib.getBooks();

		/*Filling Book's Table*/
		JSONObject booksJson = new JSONObject();
		for(int i=0;i<books.size();i++)
		{
			Map<String, String> m = new LinkedHashMap<String, String>();
			booksJson.put(books.get(i).getID(), m);
			m.put("TITLE", books.get(i).getTitle());
			m.put("AUTHOR", books.get(i).getAuthor());
			m.put("SUBJECT", books.get(i).getSubject());
			m.put("IS_ISSUED", Boolean.toString(books.get(i).getIssuedStatus()));
		}
		pw = new PrintWriter("books.json");
		pw.write(booksJson.toJSONString());
		pw.close();

		/* Filling Loan Book's Table*/
		JSONObject loanBookJson = new JSONObject();
		for(int i = 0; i < loans.size(); i++)
		{

			Map<String, String> m = new LinkedHashMap<String, String>();
			loanBookJson.put(i+1, m);
			m.put("BORROWER", Integer.toString(loans.get(i).getBorrower().getID()));
			m.put("BOOK", Integer.toString(loans.get(i).getBook().getID()));
			m.put("ISSUER", Integer.toString(loans.get(i).getIssuer().getID()));

			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

			String issueDt = df.format(loans.get(i).getIssuedDate()); 
			m.put("ISS_DATE", issueDt);

			m.put("FINE_PAID", Boolean.toString(loans.get(i).getFineStatus()));

			if(loans.get(i).getReceiver() == null)
			{
				m.put("RECEIVER", null);
				m.put("RET_DATE", null);
			}
			else
			{
				m.put("RECEIVER", Integer.toString(loans.get(i).getReceiver().getID()));
				String retDt = df.format(loans.get(i).getReturnDate());
				m.put("RET_DATE", retDt);
			}
		}
		pw = new PrintWriter("loan.json");
		pw.write(loanBookJson.toJSONString());
		pw.close();

		/* Filling On_Hold_Table*/

		int x=1;
		JSONObject onHoldJson = new JSONObject();
		for(int i=0;i<lib.getBooks().size();i++)
		{
			for(int j=0;j<lib.getBooks().get(i).getHoldRequests().size();j++)
			{
				Map<String, String> m = new LinkedHashMap<String, String>();
				onHoldJson.put(x, m);
				m.put("BOOK", Integer.toString(lib.getBooks().get(i).getHoldRequests().get(j).getBook().getID()));
				m.put("BORROWER", Integer.toString(lib.getBooks().get(i).getHoldRequests().get(j).getBorrower().getID()));

				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
				String reqDt = df.format(lib.getBooks().get(i).getHoldRequests().get(j).getRequestDate());
				m.put("REQ_DATE", reqDt);

				x++;
			}
		}
		pw = new PrintWriter("onHoldBooks.json");
		pw.write(onHoldJson.toJSONString());
		pw.close();

	} // Filling Done!  



	// Joining the data from different data files.
	// The first file in the files array should be the one with which others should match.
	private static JSONObject extractInfo(int noOfFiles, File[] files, String... reqCols) throws FileNotFoundException, IOException, ParseException {
		JSONObject finalObj = new JSONObject();

		//		int noOfFiles = files.length;
		JSONObject[] jObjs = new JSONObject[noOfFiles];
		for (int i = 0; i < noOfFiles; i++) 
		{
			jObjs[i] = (JSONObject) new JSONParser().parse(new FileReader(files[i]));
		}

		Set ids = jObjs[0].keySet();
		Iterator it = ids.iterator();

		while(it.hasNext()) 
		{
			Object ID = it.next();
			Map<String, String> m = new LinkedHashMap<String, String>();
			finalObj.put(ID, m);
			m.put("DESK_NO", jObjs[0].get(ID).toString());
			for (int i = 1; i < noOfFiles; i++) 
			{
				if (!jObjs[i].containsKey(ID)) 
				{
					continue;
				} 
				else 
				{	    			
					if (jObjs[i].get(ID).getClass().getName().equals("org.json.simple.JSONObject")) 
					{
						JSONObject jo = (JSONObject) jObjs[i].get(ID);
						for (int j = 0; j < reqCols.length; j++) 
						{
							if (jo.containsKey(reqCols[j])) 
							{
								m.put(reqCols[j], jo.get(reqCols[j]).toString());
							}
						}
					} 
					else 
					{
						for (int j = 0; j < reqCols.length; j++) 
						{
							m.put(reqCols[j], jObjs[i].get(ID).toString());
						}
					}
				}
			}
		}
		return finalObj;
	}

}   // Library Class Closed
