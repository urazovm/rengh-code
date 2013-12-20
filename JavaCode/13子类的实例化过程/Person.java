class Person
{
	String name;
	int age;
	
	Person()
	{
		System.out.println("Person的无参构造函数");
	}
	
	Person(String name, int age)
	{
		this.name = name;
		this.age = age;
		System.out.println("Person有参构造函数");
	}
	
	void eat()
	{
		System.out.println("吃饭");
	}
}