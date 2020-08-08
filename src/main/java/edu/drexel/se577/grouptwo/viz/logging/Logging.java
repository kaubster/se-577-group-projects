package edu.drexel.se577.grouptwo.viz.logging;
   
public interface Logging  { 
    public void LogCritical(String message);
 
    public void LogDebug(String message);
 
    public void LogError(String message);
}