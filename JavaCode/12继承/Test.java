class Test
{
	public static void main(String args[])
	{
		Student student1 = new Student();
		student1.name = "����";
		student1.age = 22;
		student1.grade = 3;

		student1.eat();
		student1.introduce();
		System.out.print("������" + student1.grade + "��");
		student1.study();
	}
}