package com.example.appsistema.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.appsistema.model.Backup;
import com.example.appsistema.model.Backup.EstadoBackup;

@Repository
public interface BackupRepository extends JpaRepository<Backup, Integer> {
    
    List<Backup> findByOrderByFechaBackupDesc();
    
    List<Backup> findByEstadoBackup(EstadoBackup estado);
    
    List<Backup> findByUsuario_IdUsuario(Integer idUsuario);
    
    List<Backup> findByFechaBackupBetween(LocalDateTime inicio, LocalDateTime fin);
    
    @Query("SELECT b FROM Backup b WHERE b.estadoBackup = 'EXITOSO' ORDER BY b.fechaBackup DESC")
    List<Backup> findBackupsExitosos();
    
    @Query("SELECT COUNT(b) FROM Backup b WHERE b.estadoBackup = 'EXITOSO'")
    Long countBackupsExitosos();
    
    @Query("SELECT COUNT(b) FROM Backup b WHERE b.estadoBackup = 'FALLIDO'")
    Long countBackupsFallidos();
    
    @Query("SELECT SUM(b.tamanoArchivo) FROM Backup b WHERE b.estadoBackup = 'EXITOSO'")
    Long getTamanoTotalBackups();
}