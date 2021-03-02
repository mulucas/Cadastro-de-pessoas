package curso.springboot.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	TelefoneRepository telefoneRepository;

	@Autowired
	private ReportUtil reportUtil;
	
	private ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		andView.addObject("pessoaobj", new Pessoa());
		listaPessoas();
		return andView;
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa")
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult) {

		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));

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

	@GetMapping("**/pesquisarpessoa")
	public void imprimePDF(@RequestParam("nomepesquisa") String nomepesquisa, @RequestParam("pesqsexo") String pesqsexo, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		
		if (pesqsexo != null && !pesqsexo.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.buscarPessoaPorNomeSexo(nomepesquisa, pesqsexo);
		}
		else if(nomepesquisa != null && !nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.buscarPessoaPorNome(nomepesquisa);
		}
		else if(pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.buscarPessoaPorSexo(pesqsexo);
		}
		else {
			Iterable<Pessoa> iterable = pessoaRepository.findAll();
			
			for (Pessoa pessoa : iterable) {
				pessoas.add(pessoa);
			}
		}
		
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		
		response.setContentLength(pdf.length);
		
		response.setContentType("application/octet-stream");
		
		String headerKey = "Content-Disposition";
		String headerValeu = String.format("attachment; filename=\"%s\"", "relatorio.pdf" );
		response.setHeader(headerKey, headerValeu);
		
		response.getOutputStream().write(pdf);
	}
	
	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("pesqsexo") String pesqsexo) {
		List<Pessoa> pessoas = new ArrayList<Pessoa>();

		if (pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.buscarPessoaPorNomeSexo(nomepesquisa, pesqsexo);
		} else {
			pessoas = pessoaRepository.buscarPessoaPorNome(nomepesquisa);
		}
		andView.addObject("pessoas", pessoas);
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}
}
