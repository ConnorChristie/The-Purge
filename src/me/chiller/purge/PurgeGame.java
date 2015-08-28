package me.chiller.purge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class PurgeGame
{
	private PurgeTown town;
	
	private int gameTime;
	private int peaceTime;
	
	private PurgeGameMode gamemode = PurgeGameMode.OFFGAME;
	
	private boolean isRunning;
	private boolean isCountingDown;
	
	private BukkitTask countdownTask;
	
	private Scoreboard scoreboard;
	private Objective objective;
	
	public PurgeGame(PurgeTown town, int gameTime, int peaceTime)
	{
		this.town = town;
		
		this.gameTime = gameTime;
		this.peaceTime = peaceTime;
		
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective("Purge", "dummy");
		
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public void setGamemode(PurgeGameMode gamemode)
	{
		this.gamemode = gamemode;
		
		for (PurgePlayer p : town.getOnlinePlayers())
		{
			if (gamemode == PurgeGameMode.OFFGAME)
			{
				p.resetScore();
			}
			
			p.setupScoreboard();
		}
		
		updateScoreboards();
	}
	
	public void updateScoreboards()
	{
		for (PurgePlayer p : town.getOnlinePlayers())
		{
			p.updateScoreboard();
		}
	}
	
	public void startPregame()
	{
		if (!isCountingDown)
		{
			isCountingDown = true;
			isRunning = false;
			
			teleportPlayers();
			
			setGamemode(PurgeGameMode.PREGAME);
			startCountdown(true);
		}
	}
	
	public void startGame()
	{
		if (!isRunning)
		{
			isCountingDown = true;
			isRunning = true;
			
			setGamemode(PurgeGameMode.INGAME);
			startCountdown(false);
		}
	}
	
	public void stopGame()
	{
		if (isRunning)
		{
			isCountingDown = false;
			isRunning = false;
			
			setGamemode(PurgeGameMode.OFFGAME);
			
			showDeadPlayers();
			
			for (PurgePlayer player : town.getDeadPlayers())
			{
				player.setDead(false);
			}
		}
	}
	
	private void teleportPlayers()
	{
		for (PurgePlayer player : town.getOnlinePlayers())
		{
			if (!town.getCuboid().contains(player.getPlayer().getLocation()))
			{
				player.getPlayer().teleport(player.getPlot().getGate());
				
				player.getPlayer().sendMessage(PurgeChat.info("You have been teleported back to your plot"));
			}
		}
	}
	
	public void startCountdown(final boolean isPeace)
	{
		countdownTask = Bukkit.getScheduler().runTaskAsynchronously(PurgeMain.getInstance(), new Runnable() {
			public void run()
			{
				int time = isPeace ? peaceTime : gameTime;
				String label = (isPeace ? ChatColor.DARK_GREEN + " Pre-Purge" : ChatColor.DARK_RED + " Purging");
				
				gamemode.setTitle(label + ": " + ChatColor.DARK_AQUA + (time / 60 + ":" + (time % 60 < 10 ? "0" : "") + time % 60) + "   ");
				
				//Not a mistake, update player count after all players have been added
				updateScoreboards();
				updateScoreboards();
				
				while (time >= 0 && isCountingDown)
				{
					if (isRunning)
					{
						hideDeadPlayers();
					}
					
					String timeString = time / 60 + ":" + (time % 60 < 10 ? "0" : "") + time % 60;
					
					if (isPeace)
					{
						if (time % 30 == 0 && time / 60 >= 1)
						{
							town.sendMessage(PurgeChat.info((time / 60) + " minute" + (time / 60 != 1 ? "s" : "") + (time % 60 != 0 ? ", " + (time % 60) + " second" + (time % 60 != 1 ? "s " : " ") : " ") + "remaining until The Purge!"));
						} else if ((time == 30 || time == 10 || time <= 5) && time != 0)
						{
							town.sendMessage(PurgeChat.info(time + " seconds remaining until The Purge!"));
							
							if (time <= 5 && time != 0)
							{
								town.playSound(Sound.NOTE_STICKS, 1, 0);
							}
						}
					}
					
					gamemode.setTitle(label + ": " + ChatColor.DARK_AQUA + timeString + " ");
					
					updateScoreboards();
					
					time--;
					
					if (time < 0)
					{
						if (isPeace)
						{
							town.playSound(Sound.LEVEL_UP, 1, 0);
							
							town.sendMessage(PurgeChat.info("The Purge has begun, have fun!"));
							
							startGame();
						} else
						{
							town.sendMessage(PurgeChat.info("The Purge has ended!"));
							
							stopGame();
						}
						
						break;
					}
					
					try
					{
						Thread.sleep(1000);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void hideDeadPlayers()
	{
		for (PurgePlayer player : town.getOnlinePlayers())
		{
			for (PurgePlayer deadPlayer : town.getDeadPlayers())
			{
				player.getPlayer().hidePlayer(deadPlayer.getPlayer());
			}
		}
	}
	
	public void showDeadPlayers()
	{
		for (PurgePlayer player : town.getOnlinePlayers())
		{
			for (PurgePlayer playerOther : town.getDeadPlayers())
			{
				if (!player.getPlayer().canSee(playerOther.getPlayer()))
				{
					player.getPlayer().showPlayer(playerOther.getPlayer());
				}
			}
		}
	}
	
	public void forceStop()
	{
		if (countdownTask != null)
		{
			if (isCountingDown || isRunning)
			{
				town.sendMessage(PurgeChat.info("The Purge has been forced to end..."));
			}
			
			isCountingDown = false;
			isRunning = false;
			
			setGamemode(PurgeGameMode.OFFGAME);
			
			countdownTask.cancel();
		}
	}
	
	public Scoreboard getScoreboard()
	{
		return scoreboard;
	}

	public Objective getObjective()
	{
		return objective;
	}
	
	public PurgeGameMode getGamemode()
	{
		return gamemode;
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	public boolean isAnythingRunning()
	{
		return isCountingDown || isRunning;
	}
	
	public int getPeaceTime()
	{
		return peaceTime;
	}

	public void setPeaceTime(int peaceTime)
	{
		this.peaceTime = peaceTime;
	}
}