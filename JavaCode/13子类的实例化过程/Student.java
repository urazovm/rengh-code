class Student extends Person
{
	Student()
	{
		System.out.println("Student无参构造函数");
	}
	
	Student(String name, int age)
	{
		//this.name = name;
		//this.age = age;
		super(name, age);
		System.out.println("Student的有参构造函数");
	}
}