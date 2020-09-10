package curso.springboot.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class TelefoneController {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	private ModelAndView andViewTelefone = new ModelAndView("cadastro/telefones");

	@GetMapping("/removertelefone/{idtelefone}")
	public ModelAndView excluir(@PathVariable("idtelefone") Long idtelefone) {

		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		
		telefoneRepository.deleteById(idtelefone);

		andViewTelefone.addObject("pessoaobj", pessoa);
		andViewTelefone.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		return andViewTelefone;

	}

	
	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		
		andViewTelefone.addObject("pessoaobj", pessoa.get());
		andViewTelefone.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return andViewTelefone;
	}
	
	@PostMapping("**/addfonePessoa/{pessoaid}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {
		
		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
		telefone.setPessoa(pessoa);
		telefoneRepository.save(telefone);
		
		andViewTelefone.addObject("pessoaobj", pessoa);
		andViewTelefone.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		
		return andViewTelefone;
	}

}
