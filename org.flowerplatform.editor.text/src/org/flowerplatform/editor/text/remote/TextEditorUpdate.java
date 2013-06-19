package org.flowerplatform.editor.text.remote;

/**
 * Represents a change in the content of a text file, 
 * i.e. insert, delete, replace.
 * 
 * <p>
 * Instances of this class are either:
 * 
 * <ul>
 * 	<li>sent to Flex after the file content has changed
 * 		and applied to the text displayed in text editors
 * 	<li>received from Flex after a client has edited the
 * 		text displayed in an editor and applied to the 
 * 		content of the text file
 * </ul>
 * 
 * @see UpdateFileContentClientCommand#getTextEditorUpdates()
 * @see UpdateFileContentServerCommand#getTextEditorUpdates()
 * 
 * @flowerModelElementId _4s5zwFlCEeGRrZ75u0k71A
 */
public class TextEditorUpdate {
	
	/**
	 * @see Getter doc.
	 * 
	 * @flowerModelElementId _4s5zwVlCEeGRrZ75u0k71A
	 */
	private int offset = 0;
	
	/**
	 * @see Getter doc.
	 * 
	 * @flowerModelElementId _4s6a0VlCEeGRrZ75u0k71A
	 */
	private int oldTextLength = 0;
	
	/**
	 * @see Getter doc.
	 * 
	 * @flowerModelElementId _4s6a01lCEeGRrZ75u0k71A
	 */
	private String newText = "";
	
	/**
	 * The index where the change was done.
	 */
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * The length of text that was deleted.
	 */
	public int getOldTextLength() {
		return oldTextLength;
	}
	
	public void setOldTextLength(int oldTextLength) {
		this.oldTextLength = oldTextLength;
	}
	
	/**
	 * The text that was inserted.
	 */
	public String getNewText() {
		return newText;
	}
	
	public void setNewText(String newText) {
		this.newText = newText;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TextEditorUpdate)) {
			return false;
		}
		TextEditorUpdate update = (TextEditorUpdate) obj;
		return this.offset == update.getOffset() &&
				this.oldTextLength == update.getOldTextLength() &&
				this.newText.equals(update.getNewText());
	}

	@Override
	public String toString() {
		return "TextEditorUpdate [offset=" + getOffset()
				+ ", oldTextLength=" + getOldTextLength() + ", newText="
				+ getNewText() + "]";
	}
}