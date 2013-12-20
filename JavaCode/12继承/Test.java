class Test
{
	public static void main(String args[])
	{
		Student student1 = new Student();
		student1.name = "张三";
		student1.age = 22;
		student1.grade = 3;

		student1.eat();
		student1.introduce();
		System.out.print("，我在" + student1.grade + "班");
		student1.study();
	}
}