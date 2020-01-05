package soundboard.main;

// Imports
import javax.sound.midi.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class SoundBoardApp {
	
	// MIDI Instance Variables
	private Sequencer sequencer;
	private Sequence seq;
	private Track track;
	private int[] instrumentNums = {
			35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63
			};
	
	// GUI Instance Variables
	private JFrame frame = new JFrame();
	private JPanel panel;
	private ArrayList<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
	private String[] instruments = {
			"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", 
			"Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", 
			"Vibraslap", "Low-Mid Tom", "High Agogo", "Open Hi Conga"
	};
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SoundBoardApp sba = new SoundBoardApp();
		sba.setUpGui();
	}
	
	public void start() {
		setUpGui();
	}
	
	/**
	 *  Sets up the GUI of the app and prepares the midi for usage
	 *  
	 *  @author basti
	 */
	private void setUpGui() {
		
		// Set up border
		BorderLayout border = new BorderLayout();
		panel = new JPanel(border);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Set frame content pane to panel
		frame.setContentPane(panel);
		
		// Set up the list of instrument names
		Box instrumentNames = new Box(BoxLayout.Y_AXIS);
		
		for (String instrument:instruments) {
			instrumentNames.add(new Label(instrument));
		}
		
		// Set up the buttons container
		Box buttons = new Box(BoxLayout.Y_AXIS);
		
		// Set up buttons
		JButton play = new JButton("Play Beat");
		JButton pause = new JButton("Pause Beat");
		JButton upTempo = new JButton("Increase Tempo");
		JButton downTempo = new JButton("Decrease Tempo");
		
		// Add listeners
		play.addActionListener(new playListener());
		pause.addActionListener(new pauseListener());
		upTempo.addActionListener(new tempoUpListener());
		downTempo.addActionListener(new tempoDownListener());
		
		// Add buttons to box
		buttons.add(play);
		buttons.add(pause);
		buttons.add(upTempo);
		buttons.add(downTempo);
		
		// Set up the grid of checkboxes
		GridLayout grid = new GridLayout(16, 16);
		grid.setVgap(1);
		grid.setHgap(2);
		JPanel mainPanel = new JPanel(grid);
		
		for (int i = 0; i < 256; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkBoxList.add(c);
			mainPanel.add(c);
		}
		
		// Add things to the frame
		panel.add(BorderLayout.WEST, instrumentNames);
		panel.add(BorderLayout.EAST, buttons);
		panel.add(BorderLayout.CENTER, mainPanel);
		
		// Set up the MIDI
		setupMidi();
		
		// Set up the frame
		frame.setTitle("Beat Box Sound Board");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(0, 0, 300, 300);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	private void setupMidi() {
		
		try {
			
			// Get the sequencer and open it
			sequencer = MidiSystem.getSequencer();
			sequencer.open();

			// Create a sequencer and add the track
			seq = new Sequence(Sequence.PPQ, 4);
			track = seq.createTrack();
			
		} catch (Exception ex) {ex.printStackTrace();}
		
	}
	
	private void createBeat() {
		System.out.println("Starting");
		// Create track list to store keys(0 if a note wont be played and the instrument num if it will be played)
		int[] tracklist;
		
		// Clear the previous track
		seq.deleteTrack(track);
		track = seq.createTrack();
		
		// For each row of checkboxes
		for (int i = 0; i < 16; i++) {
			
			// Create a tracklist
			tracklist = new int[16];
			
			// For each box
			for (int j = 0; j < 16; j++) {
				JCheckBox jc = (JCheckBox) checkBoxList.get(j + (i * 16));
				if (jc.isSelected() == true) {
					System.out.println("Found a beat");
					tracklist[j] = instrumentNums[i];
				} else {
					tracklist[j] = 0;
				}
			}
			
			// Make the track
			makeTracks(tracklist);
			
			// Add a note off
			track.add(makeEvent(128, 9, 0, 15, 100));
			
		}
		
		// Try to create the beat
		try {
			sequencer.setSequence(seq);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch(Exception ex) {ex.printStackTrace();}
		
	}
	
	//EVENT LISTENERS UNDER HERE
	private class playListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			System.out.println("Play");
			createBeat();
		}
		
	}
	
	private class pauseListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			System.out.println("Pause");
			sequencer.stop();
		}
		
	}
	
	private class tempoUpListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			System.out.println("Tempo Up");
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * 1.03));
		}
		
	}
	
	private class tempoDownListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			System.out.println("Tempo Down");
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * 0.97));
		}
		
	}
	
	// NEEDED FUNCTIONS GO HERE
	private void makeTracks(int[] keys) {
		
		for (int i = 0; i < 16; i++) {
			
			track.add(makeEvent(144, 9, keys[i], i, 100));
			track.add(makeEvent(128, 9, 0, i+1, 100));
			
		}
		
	}
	
	public static MidiEvent makeEvent(int cmd, int channel, int note, int beat, int hardness) {
		try {
			ShortMessage a = new ShortMessage(); // Make a music message
			
			a.setMessage(cmd, channel, note, hardness); // Play or release a note...
			MidiEvent playNote = new MidiEvent(a, beat); // ...at beat "beat"
			return playNote; // Return the note
		} catch (Exception ex) {
			// Catch and print the exception
			ex.printStackTrace();
			return null; // We still need to return something
		}
	}
}
