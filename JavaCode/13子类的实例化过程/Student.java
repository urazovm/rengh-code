class Student extends Person
{
	Student()
	{
		System.out.println("Student�޲ι��캯��");
	}
	
	Student(String name, int age)
	{
		//this.name = name;
		//this.age = age;
		super(name, age);
		System.out.println("Student���вι��캯��");
	}
}