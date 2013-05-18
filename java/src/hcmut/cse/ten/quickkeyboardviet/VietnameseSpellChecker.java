/*
 * Copyright (C) 2012 Age Of Mobile (http://www.ageofmobile.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package hcmut.cse.ten.quickkeyboardviet;

import android.util.Log;
import android.view.inputmethod.InputConnection;

public class VietnameseSpellChecker {
	public static final String INPUT_METHOD_TELEX = "Telex";
	public static final String INPUT_METHOD_VNI = "VNI";
	private static final String TAG = "VietnameseSpellChecker";
	
	/**
	 * Kiểm tra đơn giản một từ có phải là từ tiếng Việt không
	 * @param word
	 * @return
	 */
	public static boolean isVietnameseWord(CharSequence word, boolean isFreeConsonant) {
		int[][] NON_VIETNAMESE = null;
		if(isFreeConsonant) 
			NON_VIETNAMESE = NON_VIETNAMESE_CHARACTERS_2;
		else 
			NON_VIETNAMESE = NON_VIETNAMESE_CHARACTERS;
    	for (int i = word.length() - 1; i >= 0; i--) {
    		int currentChar = word.charAt(i);
    		for (int j = 0; j < NON_VIETNAMESE.length; j++) {
    			if (currentChar == NON_VIETNAMESE[j][0] || 
    				currentChar == NON_VIETNAMESE[j][0] + ('a' - 'A')) {
    				if (i >= NON_VIETNAMESE[j][1]) {
    					return false;
    				}
    			}			
    		}
    	}
    	return true;
    }
	
	/**
	 * Tự động chỉnh lại dấu và sửa các từ có "ươ", "ưa", "ơa" cho đúng
	 * @param word từ cần điều chỉnh dấu
	 * @param accent 0 = tự động tìm, 1 = sắc, 2 = huyền, 3 = hỏi, 4 = ngã, 5 = nặng
	 */
	public static boolean adjustAccent(StringBuilder word, int accent, boolean shouldFixSpecialCases) {
		int wordLength = word.length();
		int firstVowelIndex = -1;
		int vowelSequenceLength = 0;
		int accentPosition = -1;
		int currentAccentPosition = -1;
		byte numberOfAccents = 0;
		int[] vowelIndexes = new int[3];
		boolean needToFix = false;
		
		for (int pos = 0; pos < wordLength; pos++) {
			int ch = (int)word.charAt(pos);
			// check if this character is a vowel
			int vowelPosition = -1;
			for (int i = 0; i < VN_VOWELS.length; i++) {
				if (ch == VN_VOWELS[i]) {
					vowelIndexes[vowelSequenceLength] = i;
					vowelSequenceLength++;
					vowelPosition = i;
					break;
				}
			}
			
			// yes, it's a vowel
			if (vowelPosition != -1) {
				if (firstVowelIndex == -1) firstVowelIndex = pos;
				// has it have an accent?
				if (accent == 0) {
					currentAccentPosition = vowelSequenceLength - 1;
					accent = vowelPosition % 6;
				}
				if (vowelPosition % 6 > 0) {
					numberOfAccents++;
				}
			} else if (firstVowelIndex != -1) {
				// stop finding vowels
				break;
			}
			
			if (vowelSequenceLength == 3) break;
		}
		needToFix = numberOfAccents > 1;
		
		// 'gi[...]': 'i' should be considered as a consonent
		if (firstVowelIndex > 0 && vowelSequenceLength > 1) {
			int consonant = word.charAt(firstVowelIndex - 1);
			int firstVowel = VN_VOWELS[(vowelIndexes[0] / 6) * 6];
			if ((consonant == 'G' || consonant == 'g') && (firstVowel == 'i' || firstVowel == 'I')) {
				word.setCharAt(firstVowelIndex, (char)firstVowel);
				// shift right 1 character
				firstVowelIndex += 1;
				if (currentAccentPosition == 0) needToFix = true;
				currentAccentPosition -= 1;
				vowelSequenceLength -= 1;	
				vowelIndexes[0] = vowelIndexes[1];
				vowelIndexes[1] = vowelIndexes[2];							
			}
		}
						
		// fix ươ, oă cases		
		if (vowelSequenceLength >= 2) {
			int firstVowel = word.charAt(firstVowelIndex);
			firstVowel = VN_VOWELS[(vowelIndexes[0] / 6) * 6];
			int secondVowel = word.charAt(firstVowelIndex + 1);
			secondVowel = VN_VOWELS[(vowelIndexes[1] / 6) * 6];
			int consonant = 0;
			if (firstVowelIndex > 0) consonant = word.charAt(firstVowelIndex - 1);
			boolean endWithConsonant = (firstVowelIndex + 2 < wordLength);
			
			if (firstVowel == 'u' || firstVowel == 'U') {
				if ((secondVowel == 'ơ' || secondVowel == 'Ơ') && endWithConsonant
					&& (consonant != 'q' && consonant != 'Q')) {
					// uơ[...] -> ươ[...], but not quơ[...]
					vowelIndexes[0] = UW_INDEX + (firstVowel == 'u' ? 0 : 6);
					needToFix = true;
				} else if ((secondVowel == 'ă' || secondVowel == 'Ă') && 
						(consonant != 'q' && consonant != 'Q') && shouldFixSpecialCases) {
					// uă -> ưa, but not quă[...]
					vowelIndexes[0] = UW_INDEX + (firstVowel == 'u' ? 0 : 6);
					vowelIndexes[1] = A_INDEX + (secondVowel == 'ă' ? 0 : 6);
					needToFix = true;
				}
			} else if (firstVowel == 'ư' || firstVowel == 'Ư') {
				if (secondVowel == 'ơ' || secondVowel == 'Ơ') {
					if (!endWithConsonant && (currentAccentPosition == 0 || shouldFixSpecialCases)) {
						// ươ -> uơ
						vowelIndexes[0] = U_INDEX + (firstVowel == 'ư' ? 0 : 6);
						needToFix = true;
					}
				} else if (secondVowel == 'o' || secondVowel == 'O') {
					if (endWithConsonant) {
						// ưo[...] -> ươ[...]
						vowelIndexes[1] = OW_INDEX + (secondVowel == 'o' ? 0 : 6);
						needToFix = true;
					} else if (currentAccentPosition == 0 || shouldFixSpecialCases) {
						// ưo -> uơ
						vowelIndexes[0] = U_INDEX + (firstVowel == 'ư' ? 0 : 6);
						vowelIndexes[1] = OW_INDEX + (secondVowel == 'o' ? 0 : 6);
						needToFix = true;
					}
				} else if ((secondVowel == 'a' || secondVowel == 'A') && 
						(consonant == 'q' || consonant == 'Q')) {
					// qưa -> quă
					vowelIndexes[0] = U_INDEX + (firstVowel == 'ư' ? 0 : 6);
					vowelIndexes[1] = AW_INDEX + (secondVowel == 'a' ? 0 : 6);
					needToFix = true;					
				}
			}  else if (firstVowel == 'ơ' || firstVowel == 'Ơ') {
				if (secondVowel == 'a' || secondVowel == 'A') {
					// ơa -> oă
					vowelIndexes[0] = O_INDEX + (firstVowel == 'ơ' ? 0 : 6);
					vowelIndexes[1] = AW_INDEX + (secondVowel == 'a' ? 0 : 6);
					needToFix = true;
				} else if (secondVowel == 'ă' || secondVowel == 'Ă') {
					// ơă -> oă
					vowelIndexes[0] = O_INDEX + (firstVowel == 'ơ' ? 0 : 6);
					needToFix = true;
				}
			}
		}
		
		// put it to correct position
		if ((accent > 0 && vowelSequenceLength > 1) || needToFix) {
			if (vowelSequenceLength == 3) {
    			int middleVowel = word.charAt(firstVowelIndex + 1);    			
	    		middleVowel = VN_VOWELS[(vowelIndexes[1] / 6) * 6];
    			if (middleVowel != 'y' && middleVowel != 'Y') {
    				accentPosition = 1;
    			} else {
    				accentPosition = 2;
    			}
    		} else if (vowelSequenceLength == 2) {
    			accentPosition = 0;
    			
    			if (firstVowelIndex > 0) {
    				int consonant = word.charAt(firstVowelIndex - 1);
    				int firstVowel = word.charAt(firstVowelIndex);
    				firstVowel = VN_VOWELS[(vowelIndexes[0] / 6) * 6];
    				
    				if ((consonant == 'Q' || consonant == 'q') ||
    					((consonant == 'G' || consonant == 'g') && (firstVowel == 'i' || firstVowel == 'I'))) {
    					accentPosition = 1;
    				} else if (firstVowelIndex + vowelSequenceLength < wordLength) { // co phu am phia sau
        				accentPosition = 1;
    				} else if (vowelIndexes[0] > VOWEL_WITH_BREVE && vowelIndexes[1] > VOWEL_WITH_BREVE) { // 2 nguyên âm có dấu, bỏ dấu nguyên âm thứ 2
    					accentPosition = 1;
    				} else if (vowelIndexes[1] > VOWEL_WITH_BREVE) {
    					accentPosition = 1;
    				}
    			} else if (firstVowelIndex + vowelSequenceLength < wordLength) { // co phu am phia sau
    				accentPosition = 1;
    			}
    		} else { // only one vowel
    			accentPosition = 0;
    		}
			
			if (currentAccentPosition != accentPosition || needToFix) {
				for (int i = 0; i < vowelSequenceLength; i++) {
					int row = vowelIndexes[i] / 6;
		    		int replacedChar = VN_VOWELS[row * 6 + (i == accentPosition ? accent : 0)];
		    		word.setCharAt(firstVowelIndex + i, (char)replacedChar);
				}
				
				return true;
			}						
		}				
		
		return false;
	}	
	
	public static boolean adjustConsonant(StringBuilder word, boolean shiftState){
    	Log.i(TAG, "come in change_PA" + word);
		if(word.length()<2) {
			return false;
		}
		int firstChar = word.charAt(0);
		
		boolean flag_pad = false;
		for (int i = 0; i < CHARACTER_FJW.length; i++) {
			if (flag_pad == true) break;
			if (firstChar == CHARACTER_FJW[i]){
				for(int j = 0 ; j < VN_VOWELS.length ; j++){
					if( word.charAt(1) == VN_VOWELS[j]){
				
	    				if(shiftState){
	    					word.replace(0, 1, CHARACTER_FJW_REPLACE[1][i]);
	    				}else{
	    					word.replace(0, 1, CHARACTER_FJW_REPLACE[0][i]);
	    					
	    				}
	    				flag_pad = true;
	    				break;
	    				
					}
				}
				
			}
			
		}
		
		int lastChar = word.charAt(word.length()-1);
		
		boolean flag_pac = false;
		for (int i = 0; i < CHARACTER_GHK.length; i++) {
			if (flag_pac == true) break;
			if (lastChar == CHARACTER_GHK[i]){
				for(int j = 0 ; j < VN_VOWELS.length ; j++){
					if( word.charAt(word.length()-2) == VN_VOWELS[j]){
				
	    				if(shiftState){
	    					word.replace(word.length()-1, word.length(), CHARACTER_GHK_REPLACE[1][i]);
	    				}else{
	    					word.replace(word.length()-1, word.length(), CHARACTER_GHK_REPLACE[0][i]);
	    					
	    				}
	    				flag_pac = true;
	    				break;
	    				
					}
				}
				
			}
			
		}
		return flag_pad || flag_pac;
		
    }
	
	public static final int[] VN_VOWELS = {
    	'a', 'á', 'à', 'ả', 'ã', 'ạ',
    	'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
    	
    	'e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ',
    	'E', 'É', 'È', 'Ẻ', 'Ẽ', 'Ẹ',
    	
    	'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
    	'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
    	
    	'u', 'ú', 'ù', 'ủ', 'ũ', 'ụ',
    	'U', 'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ',
    	
    	'i', 'í', 'ì', 'ỉ', 'ĩ', 'ị',
    	'I', 'Í', 'Ì', 'Ỉ', 'Ĩ', 'Ị',
    	
    	'y', 'ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ',    	
    	'Y', 'Ý', 'Ỳ', 'Ỷ', 'Ỹ', 'Ỵ',
    	
    	'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',
    	'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
    	
    	'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
    	'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
    	
    	'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ',
    	'Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ',
    	
    	'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
    	'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
    	
    	'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
    	'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
    	
    	'ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự',    	
    	'Ư', 'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự'    	    	   	    	    	
    };
	
	public static final char[] VNVOWELS = {
    	'a', 'á', 'à', 'ả', 'ã', 'ạ',
    	'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
    	
    	'e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ',
    	'E', 'É', 'È', 'Ẻ', 'Ẽ', 'Ẹ',
    	
    	'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
    	'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
    	
    	'u', 'ú', 'ù', 'ủ', 'ũ', 'ụ',
    	'U', 'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ',
    	
    	'i', 'í', 'ì', 'ỉ', 'ĩ', 'ị',
    	'I', 'Í', 'Ì', 'Ỉ', 'Ĩ', 'Ị',
    	
    	'y', 'ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ',    	
    	'Y', 'Ý', 'Ỳ', 'Ỷ', 'Ỹ', 'Ỵ',
    	
    	'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',
    	'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
    	
    	'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
    	'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
    	
    	'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ',
    	'Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ',
    	
    	'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
    	'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
    	
    	'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
    	'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
    	
    	'ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự',    	
    	'Ư', 'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự'    	    	   	    	    	
    };
	
	//Chấp nhận phụ âm đầu là các từ j,w,f...
	private static final int[][] NON_VIETNAMESE_CHARACTERS_2 = {
    	{'B', 1}, {'D', 1}, {'F', 1}, {'J', 1}, {'K', 1}, {'L', 1}, {'Q', 1}, 
    	{'R', 2}, {'S', 1}, {'V', 1}, {'W', 1}, {'X', 1}, {'Z', 2}
    };
	
	private static final int[][] NON_VIETNAMESE_CHARACTERS = {
    	{'B', 1}, {'D', 1}, {'F', 0}, {'J', 0}, {'K', 1}, {'L', 1}, {'Q', 1}, 
    	{'R', 2}, {'S', 1}, {'V', 1}, {'W', 0}, {'X', 1}, {'Z', 0}
    };
		
	private static final int A_INDEX = 0;
	private static final int O_INDEX = 4 * 6;
	private static final int U_INDEX = 6 * 6;
	private static final int AW_INDEX = 14 * 6;
	private static final int OW_INDEX = 20 * 6;
	private static final int UW_INDEX = 22 * 6;
	
	private static final int VOWEL_WITH_BREVE = 12 * 6;
	
	public static final int ACCENT_AUTO = 0;
	
	private static final int[] CHARACTER_FJW = { 'f', 'j', 'w', 'F', 'J', 'W' };
	private static final String[][] CHARACTER_FJW_REPLACE =
		{ { "ph", "gi", "qu", "Ph", "Gi", "Qu" },
		  { "PH", "GI", "QU", "PH", "GI", "QU" } };
	
	private static final int[] CHARACTER_GHK = { 'g', 'h', 'k', 'G', 'H', 'K'};
	private static final String[][] CHARACTER_GHK_REPLACE = 
		{ { "ng", "nh", "ch", "Ng", "Nh", "Ch" },
		  { "NG", "NH", "CH", "NG", "NH", "CH" } };
}
