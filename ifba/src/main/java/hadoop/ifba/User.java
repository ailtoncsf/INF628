package hadoop.ifba;

public class User implements Comparable<User>{

	public Integer id;
	public String nome;
	public Integer qtdPosts;
		
	public int compareTo(User o) {
		// TODO Auto-generated method stub
		if(this.qtdPosts > o.qtdPosts)
			return -1;
		else
			return 1;
	}
	
	
	
}
