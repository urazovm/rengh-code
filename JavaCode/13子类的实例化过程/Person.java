class Person
{
	String name;
	int age;
	
	Person()
	{
		System.out.println("Person���޲ι��캯��");
	}
	
	Person(String name, int age)
	{
		this.name = name;
		this.age = age;
		System.out.println("Person�вι��캯��");
	}
	
	void eat()
	{
		System.out.println("�Է�");
	}
}