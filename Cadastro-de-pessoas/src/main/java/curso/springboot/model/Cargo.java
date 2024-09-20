package curso.springboot.model;

public enum Cargo {

	JUNIOR("Júnior"), PLENO("Pleno"), SENIOR("Sênior");

	private String nome;
	private String valor;

	private Cargo(String nome) {
		this.nome = nome;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public String toString() {
		return valor = this.name();
	}
}
