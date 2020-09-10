package curso.springboot.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.repository.PessoaRepository;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	private ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		andView.addObject("pessoaobj", new Pessoa());
		listaPessoas();
		return andView;
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa")
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
			andView.addObject("pessoaobj", pessoa);
			andView.addObject("pessoas", pessoasIt);

			List<String> msg = new ArrayList<String>();

			for (ObjectError error : bindingResult.getAllErrors()) {
				msg.add(error.getDefaultMessage());
			}
			andView.addObject("msg", msg);

			return andView;
		}

		pessoaRepository.save(pessoa);
		return listaPessoas();

	}

	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView listaPessoas() {
		Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoasIt);
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}

	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);

		andView.addObject("pessoaobj", pessoa.get());
		return andView;

	}

	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {

		pessoaRepository.deleteById(idpessoa);

		andView.addObject("pessoas", pessoaRepository.findAll());
		andView.addObject("pessoaobj", new Pessoa());
		return andView;

	}

	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa) {
		andView.addObject("pessoas", pessoaRepository.buscarPessoaPorNome(nomepesquisa));
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}
}
