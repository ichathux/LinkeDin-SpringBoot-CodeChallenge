package com.cecilireid.springchallenges;

import com.cecilireid.springchallenges.CateringJob;
import com.cecilireid.springchallenges.CateringJobRepository;
import com.cecilireid.springchallenges.Status;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("cateringJobs")
public class CateringJobController {

    private static final String IMAGE_API = "https://foodish-api.herokuapp.com";
    private final CateringJobRepository cateringJobRepository;
    WebClient client;



    public CateringJobController(CateringJobRepository cateringJobRepository, WebClient.Builder webClientBuilder) {
        this.cateringJobRepository = cateringJobRepository;
//        better way
        client = WebClient.builder().baseUrl(IMAGE_API).build();
    }

    @GetMapping
    @ResponseBody
    public List<CateringJob> getCateringJobs() {
        return cateringJobRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public CateringJob getCateringJobById(@PathVariable Long id) {
        if (cateringJobRepository.existsById(id)) {
            return cateringJobRepository.findById(id).get();
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(){
        return "Invalid ID";
    }


    @GetMapping("getCateringJobByStatus")
    public List<CateringJob> getCateringJobsByStatus(@RequestParam("status") Status status) {

        return cateringJobRepository.findAllByStatus(status);
    }

    @PostMapping
    @ResponseBody
    public CateringJob createCateringJob(@RequestBody CateringJob job) {
        job.setStatus(Status.NOT_STARTED);
        return cateringJobRepository.save(job);
    }
    @PutMapping
    @ResponseBody
    public CateringJob updateCateringJob(@RequestBody CateringJob cateringJob, @RequestParam Long id) {
        if (cateringJobRepository.existsById(id)){
            cateringJob.setId(id);
            cateringJob.setStatus(Status.NOT_STARTED);
            return cateringJobRepository.save(cateringJob);
        }else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

    }
    @PatchMapping
    public CateringJob patchCateringJob(@RequestParam("id") Long id, @RequestBody JsonNode json) {
        Optional<CateringJob> cateringJob = cateringJobRepository.findById(id);
        if (cateringJob.isPresent()){
            CateringJob cateringJob1 = cateringJob.get();
            JsonNode menu = json.get("menu");
            if (menu != null){
                cateringJob1.setMenu(menu.toString().trim());
                return cateringJobRepository.save(cateringJob1);
            }else {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            }
        }else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

    }
    @GetMapping("getImage")
    public Mono<String> getSurpriseImage() {
////can implement like this
//        WebClient client = WebClient.builder()
//                .baseUrl(IMAGE_API)
//                .defaultCookie("cookieKey", "cookieValue")
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .defaultUriVariables(Collections.singletonMap("url", IMAGE_API))
//                .build();

        return client.get().uri("/api").retrieve().bodyToMono(String.class);
    }
}
