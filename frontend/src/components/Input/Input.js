import React from 'react';
import PropTypes from 'prop-types';
import DateTime from 'react-datetime';

const DateInput = props => (
  <div className="avenir">
    <DateTime
      input={false}
      onChange={date => props.onChange(date.format('YYYY-MM-DD HH:mm'))}
    />
  </div>
);

const StandardInput = props => {
  const fontClass = props.small ? 'f5' : 'f4';
  return (
    <input
      className={`${fontClass} mid-gray pv1 mb2 w-100 input-reset outline-0 bb bt-0 br-0 bl-0 b--gray lh-copy`}
      value={props.value}
      type={props.type}
      onChange={e => props.onChange(e.target.value)}
    />
  );
}

const Input = (props) => {
  switch (props.type) {
    case 'date':
      return <DateInput {...props} />;
    default:
      return <StandardInput {...props} />;
  }
};

Input.defaultProps = {
  type: 'text',
  value: null,
  small: false,
};

Input.propTypes = {
  value: PropTypes.string,
  onChange: PropTypes.func.isRequired,
  type: PropTypes.string,
  small: PropTypes.bool,
};

DateInput.propTypes = Input.propTypes;

export default Input;
