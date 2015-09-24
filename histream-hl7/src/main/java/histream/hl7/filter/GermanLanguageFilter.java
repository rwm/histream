package histream.hl7.filter;

/**
 * Filter to translate German language specific fields to international/English language.
 * For now, only the sex code 'W' (German "Weiblich") is translated to 'F' (English "female") in PID segments.
 * 
 * @author marap1
 *
 */
public class GermanLanguageFilter extends SegmentFilter {

	@Override
	public int previewMSH(String[] msh) {
		return 1;
	}

	@Override
	protected String[] filterSegment(String[] fields) {
		String seg = fields[0];
		// translate German female sex 'W' to English 'F';
		if( seg.equals("PID") && fields.length > 8 && fields[8].length() > 0 && fields[8].charAt(0) == 'W' )fields[8]="F";
		return fields;
	}

}
