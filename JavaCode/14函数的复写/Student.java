class Student extends Person
{
	void talk()
	{
		super.talk();
		System.out.println("���ิд��������");
	}
	
	public static void main(String args[])
	{
		Student s1 = new Student();
		s1.talk();
	}
}