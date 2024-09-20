package curso.springboot.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Profissao;
import curso.springboot.model.Pessoa;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	TelefoneRepository telefoneRepository;

	@Autowired
	private ReportUtil reportUtil;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	private ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());
		listaPessoas();
		return andView;
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {

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
		
		if (file.getSize() > 0) {
			pessoa.setCurriculo(file.getBytes());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
			pessoa.setTipoFileCurriculo(file.getContentType());
		}else {
			if (pessoa.getId() != null && pessoa.getId() > 0 ) {
				
				Pessoa editPessoa = pessoaRepository.findById(pessoa.getId()).get();
				
				pessoa.setCurriculo(editPessoa.getCurriculo());
				pessoa.setNomeFileCurriculo(editPessoa.getNomeFileCurriculo());
				pessoa.setTipoFileCurriculo(editPessoa.getTipoFileCurriculo());
			}
		}

		pessoaRepository.save(pessoa);
		return listaPessoas();

	}

	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoasPorPaginacao(@PageableDefault(size = 5) Pageable pageable
			, ModelAndView modelAndView, @RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> page = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		modelAndView.addObject("pessoas",page);
		modelAndView.addObject("pessoaobj", new Pessoa());
		modelAndView.addObject("nomepesquisa", nomepesquisa);
		modelAndView.setViewName("cadastro/cadastropessoa");
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView listaPessoas() {
		Iterable<Pessoa> pessoasIt = pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome")));
		andView.addObject("pessoas", pessoasIt);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("nomepesquisa", "");
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

		listaPessoas();
		andView.addObject("pessoaobj", new Pessoa());
		return andView;

	}
	
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarCurriculo(@PathVariable("idpessoa") Long idpessoa,
			HttpServletResponse response) throws IOException {
		
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		
		if (pessoa.getCurriculo() != null) {
			
			response.setContentLength(pessoa.getCurriculo().length);
			
			response.setContentType(pessoa.getTipoFileCurriculo());
			
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
			
			response.setHeader(headerKey, headerValue);
			response.getOutputStream().write(pessoa.getCurriculo());
			
		}
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
			@RequestParam("pesqsexo") String pesqsexo,
			@PageableDefault(size = 5, sort = {"nome"}) Pageable pageable) {
		Page<Pessoa> pessoas = null;

		if (pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoaByNameSexoPage(nomepesquisa, pesqsexo, pageable);
		} else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}
		andView.addObject("pessoas", pessoas);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("nomepesquisa", nomepesquisa);
		return andView;
	}
}
