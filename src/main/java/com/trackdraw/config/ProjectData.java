package com.trackdraw.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level data class for serializing/deserializing the entire project.
 * Contains sequences and background image information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectData {
    private List<SequenceData> sequences = new ArrayList<>();
    private BackgroundImageData backgroundImage;
    
    public void setSequences(List<SequenceData> sequences) {
        this.sequences = sequences != null ? new ArrayList<>(sequences) : new ArrayList<>();
    }
}

