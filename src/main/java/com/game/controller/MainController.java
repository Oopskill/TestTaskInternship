package com.game.controller;

import com.game.entity.Player;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest/players")
public class MainController {

    @Autowired
    private PlayerService playerService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    private List<Player> getPlayers(@RequestParam Map<String, String> params){
        return playerService.getAllPlayers(params);
    }

    @GetMapping("/count")
    public Integer getCount(@RequestParam Map<String, String> params){
        return playerService.getPlayersCount(params);
    }

    @PostMapping()
    public ResponseEntity createPlayer(@RequestBody Player player){
        return playerService.createPlayer(player);
    }

    @GetMapping("/{id}")
    public ResponseEntity getPlayer(@PathVariable Long id){
        return playerService.getPlayer(id);
    }

    @PostMapping("/{id}")
    public ResponseEntity updatePlayer(@RequestBody Player player, @PathVariable Long id){
        return playerService.updatePlayer(player,id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deletePlayer(@PathVariable Long id){
        return playerService.deletePlayer(id);
    }
}
