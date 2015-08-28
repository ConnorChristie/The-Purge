package me.chiller.purge;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PurgePlayer
{
	private PurgePlot plot;
	private OfflinePlayer player;
	
	private Scoreboard scoreboard;
	private Objective objective;
	private Team team;
	
	private int score = 0;
	private int money = 0;
	private int killPoints = 0;
	
	private boolean isDead;
	
	private PurgeGameMode otherGamemode = PurgeGameMode.OFFGAME;
	
	public PurgePlayer() { }
	
	public PurgePlayer(PurgePlot plot)
	{
		this.plot = plot;
	}
	
	public PurgePlayer(String playerName)
	{
		updatePlayer(playerName);
	}
	
	public void updatePlayer()
	{
		updatePlayer(player.getName());
	}
	
	public void updatePlayer(String playerName)
	{
		player = Bukkit.getPlayerExact(playerName);
		
		if (player == null)
		{
			player = Bukkit.getOfflinePlayer(playerName);
		}
	}
	
	public Map<String, Object> serialize()
	{
		Map<String, Object> m = new HashMap<String, Object>();
		
		m.put("name", player.getName());
		m.put("kills", killPoints);
		m.put("money", money);
		
		return m;
	}
	
	public void deserialize(Map<String, Object> m)
	{
		for (String k : m.keySet())
		{
			Object v = m.get(k);
			
			if (k.equals("name"))
			{
				String playerName = (String) v;
				
				player = Bukkit.getPlayerExact(playerName);
				
				if (player == null)
				{
					player = Bukkit.getOfflinePlayer(playerName);
				}
			} else if (k.equals("kills"))
			{
				killPoints = (Integer) v;
			} else if (k.equals("money"))
			{
				money = (Integer) v;
			}
		}
	}
	
	public Player getPlayer()
	{
		updatePlayer();
		
		if (isOnline())
		{
			return (Player) player;
		}
		
		return null;
	}

	public OfflinePlayer getOfflinePlayer()
	{
		updatePlayer();
		
		return player;
	}
	
	public boolean isOnline()
	{
		updatePlayer();
		
		return player.isOnline();
	}
	
	public void setPlot(PurgePlot plot)
	{
		this.plot = plot;
	}
	
	public PurgePlot getPlot()
	{
		return plot;
	}
	
	public boolean hasPlot()
	{
		return plot != null;
	}
	
	public void setupScoreboard()
	{
		//Off game scoreboard
		
		if (!isOnline())
			return;
		
		updatePlayer();
		
		if (!hasPlot())
		{
			otherGamemode.setTitle(ChatColor.GOLD + " The Purge ");
			
			scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			objective = scoreboard.registerNewObjective("PurgeStats", "dummy");
			
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(otherGamemode.getTitle());
			
			team = scoreboard.getTeam("Stats");
			
			if (team == null)
			{
				team = scoreboard.registerNewTeam("Stats");
			}
			
			team.setPrefix(ChatColor.DARK_GREEN + "");
			team.addPlayer(Bukkit.getOfflinePlayer("Money"));
			team.addPlayer(Bukkit.getOfflinePlayer("Total Kills"));
			
			Score sc = objective.getScore(Bukkit.getOfflinePlayer("Money"));
			Score sc1 = objective.getScore(Bukkit.getOfflinePlayer("Total Kills"));
			
			sc.setScore(money);
			sc1.setScore(killPoints);
			
			getPlayer().setScoreboard(scoreboard);
			
			return;
		}
		
		switch (plot.getTown().getGame().getGamemode())
		{
			case PREGAME:
			case INGAME:
				plot.getTown().getGame().getObjective().setDisplayName(plot.getTown().getGame().getGamemode().getTitle());
				
				team = plot.getTown().getGame().getScoreboard().getTeam(player.getName());
				
				if (team == null)
				{
					team = plot.getTown().getGame().getScoreboard().registerNewTeam(player.getName());
				}
				
				team.addPlayer(player);
				team.setAllowFriendlyFire(false);
				team.setPrefix((isDead ? ChatColor.RED : ChatColor.GREEN) + "");
				
				Team pTeam = plot.getTown().getGame().getScoreboard().getTeam("Players");
				
				if (pTeam == null)
				{
					pTeam = plot.getTown().getGame().getScoreboard().registerNewTeam("Players");
				}
				
				pTeam.setPrefix(ChatColor.GOLD + "");
				pTeam.addPlayer(Bukkit.getOfflinePlayer("Players"));
				
				Score s = plot.getTown().getGame().getObjective().getScore(Bukkit.getOfflinePlayer("Players"));
				s.setScore(plot.getTown().getAlivePlayers().size());
				
				Score score = plot.getTown().getGame().getObjective().getScore(getPlayer());
				score.setScore(this.score);
				
				break;
			case OFFGAME:
				plot.getTown().getGame().getGamemode().setTitle(ChatColor.GOLD + " The Purge ");
				
				scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
				objective = scoreboard.registerNewObjective("PurgeStats", "dummy");
				
				objective.setDisplaySlot(DisplaySlot.SIDEBAR);
				objective.setDisplayName(plot.getTown().getGame().getGamemode().getTitle());
				
				team = scoreboard.getTeam("Stats");
				
				if (team == null)
				{
					team = scoreboard.registerNewTeam("Stats");
				}
				
				team.setPrefix(ChatColor.DARK_GREEN + "");
				team.addPlayer(Bukkit.getOfflinePlayer("Money"));
				team.addPlayer(Bukkit.getOfflinePlayer("Total Kills"));
				
				Score sc = objective.getScore(Bukkit.getOfflinePlayer("Money"));
				Score sc1 = objective.getScore(Bukkit.getOfflinePlayer("Total Kills"));
				
				sc.setScore(money);
				sc1.setScore(killPoints);
				
				break;
		}
		
		getPlayer().setScoreboard(plot.getTown().getGame().getGamemode() == PurgeGameMode.OFFGAME ? scoreboard : plot.getTown().getGame().getScoreboard());
	}
	
	public void updateScoreboard()
	{
		if (!isOnline())
			return;
		
		updatePlayer();
		
		if (!hasPlot())
		{
			objective.setDisplayName(otherGamemode.getTitle());
			
			Score moneyScore = objective.getScore(Bukkit.getOfflinePlayer("Money"));
			Score killsScore = objective.getScore(Bukkit.getOfflinePlayer("Total Kills"));
			
			moneyScore.setScore(money);
			killsScore.setScore(killPoints);
			
			return;
		}
		
		switch (plot.getTown().getGame().getGamemode())
		{
			case PREGAME:
			case INGAME:
				plot.getTown().getGame().getObjective().setDisplayName(plot.getTown().getGame().getGamemode().getTitle());
				
				team.setPrefix((isDead ? ChatColor.RED : ChatColor.GREEN) + "");
				
				Score s = plot.getTown().getGame().getObjective().getScore(Bukkit.getOfflinePlayer("Players"));
				s.setScore(plot.getTown().getAlivePlayers().size());
				
				Score score = plot.getTown().getGame().getObjective().getScore(getPlayer());
				score.setScore(this.score);
				
				break;
			case OFFGAME:
				plot.getTown().getGame().getGamemode().setTitle(ChatColor.GOLD + " The Purge ");
				
				objective.setDisplayName(plot.getTown().getGame().getGamemode().getTitle());
				
				Score moneyScore = objective.getScore(Bukkit.getOfflinePlayer("Money"));
				Score killsScore = objective.getScore(Bukkit.getOfflinePlayer("Total Kills"));
				
				moneyScore.setScore(money);
				killsScore.setScore(killPoints);
				
				break;
		}
	}
	
	public void resetScore()
	{
		this.score = 0;
	}

	public int getScore()
	{
		return score;
	}
	
	public void addScore(int score)
	{
		setScore(getScore() + score);
	}

	public void setScore(int score)
	{
		if (plot != null)
		{
			if (plot.getTown().getGame().isRunning())
			{
				setKillPoints(getKillPoints() + (Math.max(score, this.score) - Math.min(score, this.score)));
				setMoney(getMoney() + (Math.max(score, this.score) - Math.min(score, this.score)) * 5);
				
				this.score = score;
				
				plot.getTown().getGame().updateScoreboards();
			}
		}
	}
	
	public int getMoney()
	{
		return money;
	}
	
	public void setMoney(int money)
	{
		this.money = money;
	}
	
	public int getKillPoints()
	{
		return killPoints;
	}
	
	public void setKillPoints(int killPoints)
	{
		this.killPoints = killPoints;
	}
	
	public boolean isAlive()
	{
		return hasPlot() ? !isDead : false;
	}
	
	public boolean isAliveInGame()
	{
		return hasPlot() ? plot.getTown().getGame().getGamemode() == PurgeGameMode.INGAME && !isDead : false;
	}
	
	public boolean isDeadInGame()
	{
		return hasPlot() ? plot.getTown().getGame().getGamemode() == PurgeGameMode.INGAME && isDead : false;
	}
	
	public void setDead(boolean isDead)
	{
		this.isDead = isDead;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof PurgePlayer)
		{
			return ((PurgePlayer) other).getOfflinePlayer().getName().equals(player.getName());
		}
		
		return false;
	}
}