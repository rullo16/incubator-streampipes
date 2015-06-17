package de.fzi.cep.sepa.sources.samples.enriched;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import de.fzi.cep.sepa.commons.messaging.IMessagePublisher;
import de.fzi.cep.sepa.sources.samples.util.Utils;

public class EnrichedReplay implements Runnable {

	private IMessagePublisher publisher;
	
	public EnrichedReplay(IMessagePublisher publisher)
	{
		this.publisher = publisher;
	}
	
	@Override
	public void run() {
	
		for(int i = EnrichedReplayConfig.firstFileId; i <= 0; i++)
		{
			Optional<BufferedReader> readerOpt = Utils.getReader(makeFile(i));
			if (readerOpt.isPresent())
			{
				try {
					BufferedReader br = readerOpt.get();
					
					String line;
					long counter = 0;
					
					while ((line = br.readLine()) != null)  {
						if (counter > 0)
						{
							try {
								String[] records = line.split(",");
								
								String json = buildJsonString(records);
								//System.out.println(counter);
								publisher.onEvent(json);
								if (counter % 10000 == 0) System.out.println(counter +" Events sent.");
							} catch (Exception e) { e.printStackTrace(); }
						}
					counter++;
					}
					br.close();		
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String buildJsonString(String[] line)
	{
		StringBuilder json = new StringBuilder();
		json.append("{");
		
			json.append(Utils.toJsonstr(EnrichedReplayConfig.TIME, Long.parseLong(line[0])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.HOIST_PRESS_A, toDouble(line[1])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.HOIST_PRESS_B, toDouble(line[2])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.HOOK_LOAD, toDouble(line[3])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.IBOP, Long.parseLong(line[4])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.OIL_TEMP_GEARBOX, toDouble(line[5])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.OIL_TEMP_SWIVEL, toDouble(line[6])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.PRESSURE_GEARBOX, toDouble(line[7])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.RPM, toDouble(line[8])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.TEMP_AMBIENT, toDouble(line[9])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.TORQUE, toDouble(line[10])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.WOB, toDouble(line[11])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.MRU_POS, toDouble(line[12])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.MRU_VEL, toDouble(line[13])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.RAM_POS_MEASURED, toDouble(line[14])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.RAM_POS_SETPOINT, toDouble(line[15])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.RAM_VEL_MEASURED, toDouble(line[16])));
			json.append(Utils.toJsonstr(EnrichedReplayConfig.RAM_VEL_SETPOINT, toDouble(line[16]), false));
			
			json.append("}");		
		return json.toString();
	}

	private File makeFile(int i) {
		String filename = EnrichedReplayConfig.dataDirectory + EnrichedReplayConfig.filenamePrefix + format(i) +".csv";
		System.out.println(filename);
		return new File(filename);
	}

	private String format(int i) {
		return String.format("%03d", i);
	}
	
	private double toDouble(String field)
	{
		return Double.parseDouble(field);
	}
	
	public static void main(String[] args)
	{
		new EnrichedReplay(null).run();
	}
}
