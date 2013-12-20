class Test
{
	public static void main(String args[])
	{
		Student s1 = new Student();
		Person p1 = s1;
		p1.talk();
		s1.myTalk();
	}
}