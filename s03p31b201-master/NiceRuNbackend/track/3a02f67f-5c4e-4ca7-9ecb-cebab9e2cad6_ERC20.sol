pragma solidity ^0.5.0;

import "./Context.sol";
import "./IERC20.sol";
import "./SafeMath.sol";

contract ERC20 is Context, IERC20 {
    using SafeMath for uint256;

    mapping (address => uint256) private _balances;

    mapping (address => mapping (address => uint256)) private _allowances;

    uint256 private _totalSupply;
    // 총 토큰 발행량
    function totalSupply() public view returns (uint256) {
        return _totalSupply;
    }
    // 해당 account의 토큰 양
    function balanceOf(address account) public view returns (uint256) {
        return _balances[account];
    }
    // msg.sender로 부터 받는 사람한테 amount만큼의 토큰 전달
    function transfer(address recipient, uint256 amount) public returns (bool) {
        _transfer(_msgSender(), recipient, amount);
        return true;
    }
    // owner 계정에서, spender에게 쓰게 해준 토큰의 양 return
    function allowance(address owner, address spender) public view returns (uint256) {
        return _allowances[owner][spender];
    }
    // owner가 spender에게 amount만큼 쓰게 해줌.
    function approve(address spender, uint256 amount) public returns (bool) {
        _approve(_msgSender(), spender, amount);
        return true;
    }
    // sender가 recipient에게 특정한 양의 토큰을 전달해줌.
    function transferFrom(address sender, address recipient, uint256 amount) public returns (bool) {
        _transfer(sender, recipient, amount);
        _approve(sender, _msgSender(), _allowances[sender][_msgSender()].sub(amount, "ERC20: transfer amount exceeds allowance"));
        return true;
    }

    // owner계정에서 spender가 가져가는 양을 늘려줌.
    function increaseAllowance(address spender, uint256 addedValue) public returns (bool) {
        _approve(_msgSender(), spender, _allowances[_msgSender()][spender].add(addedValue));
        return true;
    }

    // owner계정에서 spender가 가져갈 수 있는 양을 줄여줌.
    function decreaseAllowance(address spender, uint256 subtractedValue) public returns (bool) {
        _approve(_msgSender(), spender, _allowances[_msgSender()][spender].sub(subtractedValue, "ERC20: decreased allowance below zero"));
        return true;
    }

    // sender에서 recipient로 특정 amount만큼 전달하는 내부함수.
    function _transfer(address sender, address recipient, uint256 amount) internal {
        require(sender != address(0), "ERC20: transfer from the zero address");
        require(recipient != address(0), "ERC20: transfer to the zero address");

        _balances[sender] = _balances[sender].sub(amount, "ERC20: transfer amount exceeds balance");
        _balances[recipient] = _balances[recipient].add(amount);
        emit Transfer(sender, recipient, amount);
    }
    // 특정 account에서 amount만큼의 token을 만든다.
    function _mint(address account, uint256 amount) internal {
        require(account != address(0), "ERC20: mint to the zero address");

        _totalSupply = _totalSupply.add(amount);
        _balances[account] = _balances[account].add(amount);
        emit Transfer(address(0), account, amount);
    }

    // 특정 account에서 amount만큼의 token을 없앤다.
    function _burn(address account, uint256 amount) internal {
        require(account != address(0), "ERC20: burn from the zero address");

        _balances[account] = _balances[account].sub(amount, "ERC20: burn amount exceeds balance");
        _totalSupply = _totalSupply.sub(amount);
        emit Transfer(account, address(0), amount);
    }

    // owner가 spender에게 amount만큼 쓰게 해주는 내부함수.
    function _approve(address owner, address spender, uint256 amount) internal {
        require(owner != address(0), "ERC20: approve from the zero address");
        require(spender != address(0), "ERC20: approve to the zero address");

        _allowances[owner][spender] = amount;
        emit Approval(owner, spender, amount);
    }
    // 특정 account에서 amount만큼의 token을 없앤다.
    function _burnFrom(address account, uint256 amount) internal {
        _burn(account, amount);
        _approve(account, _msgSender(), _allowances[account][_msgSender()].sub(amount, "ERC20: burn amount exceeds allowance"));
    }
}
