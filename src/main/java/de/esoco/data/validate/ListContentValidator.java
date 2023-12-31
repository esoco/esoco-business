//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.data.validate;

import java.util.List;

/**
 * A validator that checks the content of a list by checking each element with
 * another validator.
 *
 * @author eso
 */
public class ListContentValidator<T> implements Validator<List<T>> {

	private static final long serialVersionUID = 1L;

	private Validator<? super T> rElementValidator;

	/**
	 * Creates a new instance.
	 *
	 * @param rElementValidator The validator for the list elements
	 */
	public ListContentValidator(Validator<? super T> rElementValidator) {
		this.rElementValidator = rElementValidator;
	}

	/**
	 * Default constructor for serialization.
	 */
	ListContentValidator() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid(List<T> rList) {
		if (rList == null) {
			return false;
		}

		for (T rElement : rList) {
			if (!rElementValidator.isValid(rElement)) {
				return false;
			}
		}

		return true;
	}
}
