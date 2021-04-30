package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ResponseEntity<Player> getPlayer(Long id){
        ResponseEntity<Player> notValid = getNotValidId(id);
        if (notValid != null)
            return notValid;

        return ResponseEntity.ok(playerRepository.findById(id).get());
    }

    public List<Player> getAllPlayers(Map<String, String > params){
        List<Player> players;
        int size = Integer.parseInt(params.getOrDefault("pageSize","3"));
        int from = size * Integer.parseInt(params.getOrDefault("pageNumber","0"));
        players = entityManager.createQuery(getCriteriaQuery(params)).setFirstResult(from).setMaxResults(size).getResultList();
        return players;
    }

    public ResponseEntity<Player> createPlayer(Player player){
        if (!isValidPlayer(player))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        if (player.getBanned() == null)
            player.setBanned(false);

        player.updateCharacteristics();
        playerRepository.saveAndFlush(player);
        return ResponseEntity.ok(player);
    }

    public ResponseEntity<Player> deletePlayer(Long id){
        ResponseEntity<Player> notValid = getNotValidId(id);
        if (notValid != null)
            return notValid;

        playerRepository.delete(playerRepository.findById(id).get());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    public ResponseEntity<Player> updatePlayer(Player player, Long id){
        ResponseEntity<Player> notValid = getNotValidId(id);
        if (notValid != null)
            return notValid;

        Player updatingPlayer = playerRepository.findById(id).get();

        if (player.getName() != null) updatingPlayer.setName(player.getName());
        if (player.getTitle() != null) updatingPlayer.setTitle(player.getTitle());
        if (player.getRace() != null) updatingPlayer.setRace(player.getRace());
        if (player.getProfession() != null) updatingPlayer.setProfession(player.getProfession());
        if (player.getBirthday() != null) updatingPlayer.setBirthday(player.getBirthday());
        if (player.getBanned() != null) updatingPlayer.setBanned(player.getBanned());
        if (player.getExperience() != null) updatingPlayer.setExperience(player.getExperience());

        updatingPlayer.updateCharacteristics();

        if (!isValidPlayer(updatingPlayer))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        playerRepository.save(updatingPlayer);
        return ResponseEntity.status(HttpStatus.OK).body(updatingPlayer);
    }

    public int getPlayersCount(Map<String, String> params) {
        return entityManager.createQuery(getCriteriaQuery(params)).getResultList().size();
    }

    public CriteriaQuery<Player> getCriteriaQuery(Map<String, String> params){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Player> cq = builder.createQuery(Player.class);
        Root<Player> root = cq.from(Player.class);
        List<Predicate> conditions = getPredicates(builder,root,params);
        String order = PlayerOrder.valueOf(params.getOrDefault("order","ID")).getFieldName();

        cq.where(builder.and(conditions.toArray(new Predicate[conditions.size()]))).orderBy(builder.asc(root.get(order)));

        return cq;
    }

    public List<Predicate> getPredicates(CriteriaBuilder builder,Root<Player> root,Map<String, String > params){
        List<Predicate> conditions = new ArrayList<>();
        conditions.add(builder.like(root.get("name"),"%" + params.getOrDefault("name","") + "%"));
        conditions.add(builder.like(root.get("title"),"%" + params.getOrDefault("title","") + "%"));

        if(params.containsKey("after")){
            conditions.add(builder.greaterThanOrEqualTo(root.get("birthday"),new Date(Long.parseLong(params.get("after")))));
        }

        if(params.containsKey("before")){
            conditions.add(builder.lessThanOrEqualTo(root.get("birthday"),new Date(Long.parseLong(params.get("before")))));
        }

        if(params.containsKey("banned")){
            conditions.add(builder.equal(root.get("banned"),Boolean.parseBoolean(params.get("banned"))));
        }

        if(params.containsKey("minExperience")){
            conditions.add(builder.greaterThanOrEqualTo(root.get("experience"),Integer.parseInt(params.get("minExperience"))));
        }

        if(params.containsKey("maxExperience")){
            conditions.add(builder.lessThanOrEqualTo(root.get("experience"),Integer.parseInt(params.get("maxExperience"))));
        }

        if(params.containsKey("minLevel")){
            conditions.add(builder.greaterThanOrEqualTo(root.get("level"),Integer.parseInt(params.get("minLevel"))));
        }

        if(params.containsKey("maxLevel")){
            conditions.add(builder.lessThanOrEqualTo(root.get("level"),Integer.parseInt(params.get("maxLevel"))));
        }

        if (params.containsKey("race")){
            conditions.add(builder.equal(root.get("race"),Race.valueOf(params.get("race"))));
        }

        if (params.containsKey("profession")){
            conditions.add(builder.equal(root.get("profession"),Profession.valueOf(params.get("profession"))));
        }
        return conditions;
    }

    public boolean isValidPlayer(Player player){
        if (player.getName() == null || player.getExperience() == null || player.getBirthday() == null||
                player.getTitle() == null || player.getProfession() == null || player.getRace() == null){
            return false;
        }
        if (player.getName().isEmpty() || player.getName().length() > 12 || player.getTitle().length() > 30 ||
                player.getBirthday().getTime() > new GregorianCalendar(3000,0,1).getTimeInMillis() ||
                player.getBirthday().getTime() < new GregorianCalendar(2000,0,1).getTimeInMillis() ||
                player.getExperience() < 0 || player.getExperience() > 10_000_000){
            return false;
        }
        return true;
    }

    public ResponseEntity<Player> getNotValidId(Long id){
        if (id < 1 || id % 1 != 0)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        if (!playerRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        return null;
    }
}
