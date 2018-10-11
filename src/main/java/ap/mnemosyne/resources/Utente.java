package ap.mnemosyne.resources;

public class Utente
{
	private final String nome, cognome, passw;

	public Utente(String nome, String cognome, String passw)
	{
		this.nome = nome;
		this.cognome = cognome;
		this.passw = passw;
	}
	
	public final String getNome()
	{
		return nome;
	}

	public final String getCognome()
	{
		return cognome;
	}

	public final String getPassw()
	{
		return passw;
	}
}
